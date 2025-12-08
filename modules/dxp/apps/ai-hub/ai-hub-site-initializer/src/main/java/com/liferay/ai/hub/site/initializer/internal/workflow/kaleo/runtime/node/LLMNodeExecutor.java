/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node;

import com.liferay.ai.hub.embedding.store.EmbeddingStoreFactory;
import com.liferay.ai.hub.embedding.store.EmbeddingStoreFactoryRegistry;
import com.liferay.ai.hub.mcp.tool.provider.MCPToolProviderFactory;
import com.liferay.ai.hub.site.initializer.internal.assistant.handler.AssistantHandlerContext;
import com.liferay.ai.hub.site.initializer.internal.assistant.handler.AssistantHandlerUtil;
import com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.util.InputVariablesUtil;
import com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.util.ToolsUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
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

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.io.Serializable;

import java.net.MalformedURLException;
import java.net.URL;

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

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		Map<String, String> kaleoNodeSettingValues = new HashMap<>();

		List<KaleoNodeSetting> kaleoNodeSettings =
			_kaleoNodeSettingLocalService.getKaleoNodeSettings(
				currentKaleoNode.getKaleoNodeId());

		for (KaleoNodeSetting kaleoNodeSetting : kaleoNodeSettings) {
			kaleoNodeSettingValues.put(
				kaleoNodeSetting.getName(), kaleoNodeSetting.getValue());
		}

		ServiceContext serviceContext = executionContext.getServiceContext();

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
			).contentRetriever(
				_createContentRetriever()
			).invocationParameters(
				InvocationParameters.from(
					Map.of(
						"executionContext", executionContext,
						"permissionChecker",
						PermissionThreadLocal.getPermissionChecker()))
			).memoryId(
				GetterUtil.getString(workflowContext.get("memoryId"))
			).onCompleteResponse(
				response -> _completeResponse(
					response, executionContext, currentKaleoNode,
					vertexAiGeminiStreamingChatModel)
			).onError(
				throwable -> vertexAiGeminiStreamingChatModel.close()
			).systemMessageProvider(
				object -> InputVariablesUtil.applyInputVariables(
					executionContext, "prompt", kaleoNodeSettingValues)
			).toolProvider(
				_mcpToolProviderFactory.create(
					kaleoInstanceToken.getCompanyId(),
					kaleoInstanceToken.getGroupId(), serviceContext.getLocale(),
					ToolsUtil.getMCPServerExternalReferenceCodes(
						_jsonFactory, kaleoNodeSettingValues),
					serviceContext.getUserId())
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
		KaleoNode kaleoNode,
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

			List<KaleoTransition> kaleoTransitions =
				kaleoNode.getKaleoTransitions();

			KaleoTransition kaleoTransition = kaleoTransitions.get(0);

			_workflowNodeManager.completeWorkflowNode(
				kaleoInstanceToken.getCompanyId(),
				kaleoInstanceToken.getUserId(),
				kaleoInstanceToken.getKaleoInstanceTokenId(),
				kaleoTransition.getName(), workflowContext, false);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
		finally {
			vertexAiGeminiStreamingChatModel.close();
		}
	}

	private ContentRetriever _createContentRetriever() {
		Document document = _loadDocument();

		DocumentSplitter documentSplitter = DocumentSplitters.recursive(300, 0);

		List<TextSegment> textSegments = documentSplitter.split(document);

		EmbeddingModel embeddingModel = VertexAiEmbeddingModel.builder(
		).location(
			"us-central1"
		).modelName(
			"gemini-embedding-001"
		).project(
			"ai-hub-liferay"
		).publisher(
			"google"
		).build();

		List<Embedding> embeddings = embeddingModel.embedAll(
			textSegments
		).content();

		EmbeddingStoreFactory embeddingStoreFactory =
			_embeddingStoreFactoryRegistry.getEmbeddingStoreFactory(
				"in-memory");

		EmbeddingStore<TextSegment> embeddingStore =
			embeddingStoreFactory.create();

		embeddingStore.addAll(embeddings, textSegments);

		return EmbeddingStoreContentRetriever.builder(
		).embeddingStore(
			embeddingStore
		).embeddingModel(
			embeddingModel
		).maxResults(
			2
		).minScore(
			0.5
		).build();
	}

	private Document _loadDocument() {
		try {
			return UrlDocumentLoader.load(
				new URL(
					"https://gist.githubusercontent.com/MarinhoFeliphe/3790737933594e76f697bb7e834473b9/raw/b1e0ded2b9921de222476e81323f9b42890dcb9e/about-feliphe-marinho.txt"),
				new TextDocumentParser());
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Reference
	private EmbeddingStoreFactoryRegistry _embeddingStoreFactoryRegistry;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private KaleoNodeSettingLocalService _kaleoNodeSettingLocalService;

	@Reference
	private MCPToolProviderFactory _mcpToolProviderFactory;

	@Reference
	private WorkflowNodeManager _workflowNodeManager;

}