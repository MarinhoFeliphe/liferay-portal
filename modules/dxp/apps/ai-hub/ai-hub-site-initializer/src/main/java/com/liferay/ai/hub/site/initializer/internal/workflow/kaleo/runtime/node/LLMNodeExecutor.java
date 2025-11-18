/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node;

import com.liferay.ai.hub.site.initializer.internal.assistant.handler.AssistantHandlerContext;
import com.liferay.ai.hub.site.initializer.internal.assistant.handler.AssistantHandlerUtil;
import com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.util.InputVariablesUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.workflow.kaleo.definition.NodeType;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.model.KaleoNodeSetting;
import com.liferay.portal.workflow.kaleo.model.KaleoTransition;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.graph.PathElement;
import com.liferay.portal.workflow.kaleo.runtime.node.BaseNodeExecutor;
import com.liferay.portal.workflow.kaleo.runtime.node.NodeExecutor;
import com.liferay.portal.workflow.kaleo.service.KaleoNodeSettingLocalService;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(service = NodeExecutor.class)
public class LLMNodeExecutor extends BaseNodeExecutor {

	@Override
	public NodeType getNodeType() {
		return NodeType.LLM;
	}

	@Override
	protected boolean doEnter(
		KaleoNode currentKaleoNode, ExecutionContext executionContext) {

		return true;
	}

	@Override
	protected void doExecute(
			KaleoNode currentKaleoNode, ExecutionContext executionContext,
			List<PathElement> remainingPathElements)
		throws PortalException {

		Map<String, String> kaleoNodeSettingValues = new HashMap<>();

		List<KaleoNodeSetting> kaleoNodeSettings =
			_kaleoNodeSettingLocalService.getKaleoNodeSettings(
				currentKaleoNode.getKaleoNodeId());

		for (KaleoNodeSetting kaleoNodeSetting : kaleoNodeSettings) {
			kaleoNodeSettingValues.put(
				kaleoNodeSetting.getName(), kaleoNodeSetting.getValue());
		}

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		WorkflowInstance workflowInstance =
			_workflowInstanceManager.getWorkflowInstance(
				CompanyThreadLocal.getCompanyId(),
				kaleoInstanceToken.getKaleoInstanceId());

		Group group = _groupLocalService.fetchGroup(
			workflowInstance.getGroupId());

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_MCP_SERVER", CompanyThreadLocal.getCompanyId());

		Page<ObjectEntry> page = null;

		try {
			page = _objectEntryManager.getObjectEntries(
				CompanyThreadLocal.getCompanyId(), objectDefinition,
				group.getGroupKey(), null,
				new DefaultDTOConverterContext(
					false, Collections.emptyMap(), _dtoConverterRegistry, null,
					LocaleUtil.getDefault(), null,
					_userService.getUserById(kaleoInstanceToken.getUserId())),
				(String)null, null, null, null);
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		List<ObjectEntry> list = (List<ObjectEntry>)page.getItems();

		List<McpClient> mcpClients = new ArrayList<>();

		for (ObjectEntry objectEntry : list) {
			McpTransport mcpTransport = new HttpMcpTransport.Builder(
			).sseUrl(
				""
			).customHeaders(
				Map.of("Authorization", _getAuthorization("", "", ""))
			).build();

			McpClient mcpClient = new DefaultMcpClient.Builder(
			).transport(
				mcpTransport
			).build();

			mcpClients.add(mcpClient);
		}

		McpToolProvider toolProvider = McpToolProvider.builder(
		).mcpClients(
			mcpClients
		).build();

		VertexAiGeminiStreamingChatModel vertexAiGeminiStreamingChatModel =
			VertexAiGeminiStreamingChatModel.builder(
			).project(
				"ai-hub-liferay"
			).location(
				"us-central1"
			).modelName(
				"gemini-2.5-flash-lite"
			).build();

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		AssistantHandlerUtil.handle(
			AssistantHandlerContext.builder(
			).memoryId(
				GetterUtil.getString(workflowContext.get("memoryId"))
			).onCompleteResponse(
				response -> _completeResponse(
					response, executionContext,
					vertexAiGeminiStreamingChatModel)
			).onError(
				throwable -> vertexAiGeminiStreamingChatModel.close()
			).systemMessageProvider(
				object -> InputVariablesUtil.applyInputVariables(
					executionContext, "prompt", kaleoNodeSettingValues)
			).toolProvider(
				toolProvider
			).userMessage(
				InputVariablesUtil.applyInputVariables(
					executionContext, "userMessage", kaleoNodeSettingValues)
			).vertexAiGeminiStreamingChatModel(
				vertexAiGeminiStreamingChatModel
			).build(),
			GetterUtil.getString(
				workflowContext.get("assistantKey"), "default"));
	}

	@Override
	protected void doExit(
			KaleoNode currentKaleoNode, ExecutionContext executionContext,
			List<PathElement> remainingPathElements)
		throws PortalException {

		KaleoTransition kaleoTransition = null;

		if (Validator.isNull(executionContext.getTransitionName())) {
			kaleoTransition = currentKaleoNode.getDefaultKaleoTransition();
		}
		else {
			kaleoTransition = currentKaleoNode.getKaleoTransition(
				executionContext.getTransitionName());
		}

		remainingPathElements.add(
			new PathElement(
				null, kaleoTransition.getTargetKaleoNode(),
				new ExecutionContext(
					executionContext.getKaleoInstanceToken(),
					executionContext.getWorkflowContext(),
					executionContext.getServiceContext())));
	}

	private void _completeResponse(
		ChatResponse chatResponse, ExecutionContext executionContext,
		VertexAiGeminiStreamingChatModel vertexAiGeminiStreamingChatModel) {

		try {
			Map<String, Serializable> workflowContext =
				executionContext.getWorkflowContext();

			BiConsumer<String, String> biConsumer =
				(BiConsumer)workflowContext.get("sendOutBoundEvent");

			AiMessage aiMessage = chatResponse.aiMessage();

			biConsumer.accept(
				aiMessage.text(),
				GetterUtil.getString(workflowContext.get("outBoundEventName")));

			KaleoInstanceToken kaleoInstanceToken =
				executionContext.getKaleoInstanceToken();

			_workflowNodeManager.completeWorkflowNode(
				kaleoInstanceToken.getCompanyId(),
				kaleoInstanceToken.getUserId(),
				kaleoInstanceToken.getKaleoInstanceTokenId(), "end",
				workflowContext, false);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
		finally {
			vertexAiGeminiStreamingChatModel.close();
		}
	}

	private String _getAuthorization(
		String authorizationType, String password, String userName) {

		try {
			Base64.Encoder encoder = Base64.getEncoder();

			String userNameAndPassword = userName + ":" + password;

			return authorizationType + " " +
				new String(
					encoder.encode(userNameAndPassword.getBytes("UTF-8")),
					"UTF-8");
		}
		catch (UnsupportedEncodingException unsupportedEncodingException) {
			throw new RuntimeException(unsupportedEncodingException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LLMNodeExecutor.class);

	@Reference
	private static DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private KaleoNodeSettingLocalService _kaleoNodeSettingLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryManager _objectEntryManager;

	@Reference
	private UserService _userService;

	@Reference
	private WorkflowInstanceManager _workflowInstanceManager;

	@Reference
	private WorkflowNodeManager _workflowNodeManager;

}