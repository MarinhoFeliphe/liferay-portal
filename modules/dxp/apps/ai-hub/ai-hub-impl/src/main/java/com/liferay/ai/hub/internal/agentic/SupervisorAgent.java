/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agentic;

import com.liferay.ai.hub.agentic.Agent;
import com.liferay.ai.hub.agentic.AgentContext;
import com.liferay.ai.hub.internal.web.search.LiferayWebSearchEngine;
import com.liferay.ai.hub.rest.resource.v1_0.util.SseUtil;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.workflow.constants.WorkflowDefinitionConstants;
import com.liferay.portal.workflow.instance.WorkflowInstanceActionExecutor;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

import java.io.Serializable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author João Victor Alves
 */
@Component(service = Agent.class)
public class SupervisorAgent implements Agent {

	@Override
	public void invoke(AgentContext agentContext) {
		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(
				SupervisorAgent.class.getName());

		executorService.submit(
			() -> {
				try (VertexAiGeminiChatModel vertexAiGeminiChatModel =
						VertexAiGeminiChatModel.builder(
						).location(
							"us-central1"
						).modelName(
							"gemini-2.5-flash-lite"
						).project(
							"ai-hub-liferay"
						).logRequests(
							true
						).logResponses(
							true
						).build()) {

					_invoke(agentContext, vertexAiGeminiChatModel);
				}
				catch (Exception exception) {
					_log.error(exception);
				}
			});
	}

	public interface LiferayKnowledgeAgent {

		@dev.langchain4j.agentic.Agent(
			description = "A Liferay Knowledge Agent that assists users by retrieving accurate information specifically from the Liferay Digital Experience Platform (DXP) instance.",
			outputKey = "response"
		)
		@SystemMessage(
			"You are a Liferay Knowledge Agent, a specialized AI agent designed to assist users by retrieving accurate information specifically from the Liferay Digital Experience Platform (DXP) instance or using the chat history. Your tone is professional, helpful, and concise."
		)
		@UserMessage("{{request}}")
		public String invokeLiferayKnowledgeAgent(@V("request") String request);

	}

	public class MakeLongerAgent {

		@dev.langchain4j.agentic.Agent(
			"This writing agent takes a given text and expands it into a longer, more detailed version."
		)
		public String invokeMakeLongerAgent(@V("text") String text) {
			try {
				CompletableFuture<String> completableFuture =
					new CompletableFuture<>();

				WorkflowInstance workflowInstance =
					_workflowInstanceManager.startWorkflowInstance(
						51875351353867L, 0L, 20132L,
						WorkflowDefinitionConstants.NAME_MAKE_LONGER, 1, null,
						HashMapBuilder.<String, Serializable>put(
							WorkflowConstants.CONTEXT_SERVICE_CONTEXT,
							new ServiceContext()
						).put(
							"text", text
						).build());

				_workflowInstanceActionExecutor.addCompletionAction(
					workflowInstance.getWorkflowInstanceId(),
					workflowContext -> completableFuture.complete(
						GetterUtil.getString(
							workflowContext.get("rewrittenText"))));

				return completableFuture.get(15, TimeUnit.SECONDS);
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}

	}

	private void _invoke(
		AgentContext agentContext,
		VertexAiGeminiChatModel vertexAiGeminiChatModel) {

		LiferayKnowledgeAgent liferayKnowledgeAgent =
			AgenticServices.agentBuilder(
				LiferayKnowledgeAgent.class
			).chatMemoryProvider(
				id -> MessageWindowChatMemory.builder(
				).chatMemoryStore(
					_inMemoryChatMemoryStore
				).id(
					id
				).maxMessages(
					30
				).build()
			).chatModel(
				vertexAiGeminiChatModel
			).contentRetriever(
				WebSearchContentRetriever.builder(
				).webSearchEngine(
					new LiferayWebSearchEngine()
				).build()
			).build();

		dev.langchain4j.agentic.supervisor.SupervisorAgent supervisorAgent =
			AgenticServices.supervisorBuilder(
			).chatModel(
				vertexAiGeminiChatModel
			).subAgents(
				liferayKnowledgeAgent, new MakeLongerAgent()
			).responseStrategy(
				SupervisorResponseStrategy.SUMMARY
			).build();

		SseUtil.send(
			supervisorAgent.invoke(
				MapUtil.getString(agentContext.getInput(), "message")),
			"Chat Message Sent", agentContext.getSseEventSinkKey());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SupervisorAgent.class);

	private final InMemoryChatMemoryStore _inMemoryChatMemoryStore =
		new InMemoryChatMemoryStore();

	@Reference
	private PortalExecutorManager _portalExecutorManager;

	@Reference
	private WorkflowInstanceActionExecutor _workflowInstanceActionExecutor;

	@Reference
	private WorkflowInstanceManager _workflowInstanceManager;

}