/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agent;

import com.liferay.ai.hub.agent.AgentContext;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.workflow.constants.WorkflowDefinitionConstants;
import com.liferay.portal.workflow.instance.WorkflowInstanceActionExecutor;
import dev.langchain4j.agentic.internal.AgentSpecsProvider;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.service.V;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Feliphe Marinho
 */
public class ChatMessagePipelineAgent implements AgentSpecsProvider {

	private final long _companyId = 0L;
	private final long _groupId = 0L;
	private final long _userId = 0L;
	private final WorkflowInstanceManager _workflowInstanceManager;
	private final WorkflowInstanceActionExecutor _workflowInstanceActionExecutor;

	public ChatMessagePipelineAgent(
		AgentContext agentContext,
		WorkflowInstanceActionExecutor workflowInstanceActionExecutor,
		WorkflowInstanceManager workflowInstanceManager) {

		// _companyId = agentContext.getCompanyId();
		// _groupId = agentContext.getGroupId();
		// _userId = agentContext.getUserId();
		_workflowInstanceActionExecutor = workflowInstanceActionExecutor;
		_workflowInstanceManager = workflowInstanceManager;
	}

	@dev.langchain4j.agentic.Agent
	public String invoke(Map<String, Object> input) {
		try {
			CompletableFuture<String> completableFuture =
				new CompletableFuture<>();

			WorkflowInstance workflowInstance =
				_workflowInstanceManager.startWorkflowInstance(
					_companyId, _groupId, _userId,
					WorkflowDefinitionConstants.NAME_CHAT_MESSAGE_PIPELINE, 1, null,
					HashMapBuilder.<String, Serializable>put(
						WorkflowConstants.CONTEXT_SERVICE_CONTEXT,
						new ServiceContext()
					).put(
						inputKey(), GetterUtil.getString(input.get(inputKey()))
					).build());

			_workflowInstanceActionExecutor.addCompletionAction(
				workflowInstance.getWorkflowInstanceId(),
				workflowContext -> completableFuture.complete(
					GetterUtil.getString(
						workflowContext.get(outputKey()))));

			return completableFuture.get(10, TimeUnit.SECONDS);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public String inputKey() {
		return "userMessage";
	}

	@Override
	public String outputKey() {
		return "assistantResponse";
	}

	@Override
	public String description() {
		return "This agent provides targeted support by searching the Liferay DXP instance for the most relevant data, ensuring every response is grounded in your specific environment.";
	}

	@Override
	public boolean async() {
		return false;
	}

	@Override
	public AgentListener listener() {
		return null;
	}
}
