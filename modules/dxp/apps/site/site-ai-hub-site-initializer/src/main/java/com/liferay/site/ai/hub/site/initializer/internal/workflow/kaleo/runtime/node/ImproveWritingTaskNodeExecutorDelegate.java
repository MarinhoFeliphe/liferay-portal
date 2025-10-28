/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.graph.PathElement;
import com.liferay.portal.workflow.kaleo.runtime.node.TaskNodeExecutorDelegate;
import org.osgi.service.component.annotations.Component;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import org.osgi.service.component.annotations.Reference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Feliphe Marinho
 */
@Component(service = TaskNodeExecutorDelegate.class)
public class ImproveWritingTaskNodeExecutorDelegate implements
	TaskNodeExecutorDelegate {

	@Override
	public void execute(
		KaleoNode currentKaleoNode, ExecutionContext executionContext,
		List<PathElement> remainingPathElements) throws PortalException {

		if (!Objects.equals(currentKaleoNode.getName(), getKey())) {
			return;
		}

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		Map<String, Serializable> content =
			(Map<String, Serializable>)workflowContext.get("content");

		VertexAiGeminiStreamingChatModel model =
			VertexAiGeminiStreamingChatModel.builder()
				.project(_PROJECT)
				.location(_LOCATION)
				.modelName(_MODEL_NAME)
				.build();

		Assistant assistant = AiServices.builder(
			Assistant.class
		).streamingChatModel(
			model
		).build();

		TokenStream tokenStream = assistant.rewrite(
			GetterUtil.getString(content.get("value")));

		tokenStream
			.onCompleteResponse(
				response -> {
					System.out.println("Thread: " + Thread.currentThread().getId());
					System.out.println(response.aiMessage().text());

					_updateWorkflowContext(response, executionContext);

					model.close();
				})
			.onError(
				throwable -> {
					System.out.println("Thread: " + Thread.currentThread().getId());
					System.out.println(throwable.getMessage());

					model.close();
				}
			)
			.start();
	}

	private void _updateWorkflowContext(
		ChatResponse chatResponse, ExecutionContext executionContext) {

		try {
			Map<String, Serializable> workflowContext =
				executionContext.getWorkflowContext();

			workflowContext.put(
				"response", chatResponse.aiMessage().text());

			KaleoInstanceToken kaleoInstanceToken =
				executionContext.getKaleoInstanceToken();

			_workflowInstanceManager.updateWorkflowContext(
				kaleoInstanceToken.getCompanyId(),
				kaleoInstanceToken.getKaleoInstanceId(), workflowContext);
		}
		catch (WorkflowException exception) {
			System.out.println(exception.getMessage());
		}
	}

	@Override
	public String getKey() {
		return "improveWriting";
	}

	public interface Assistant {

		@SystemMessage("""
       You are a professional writing editor.
       
       Your sole task is to take the provided text and rewrite it to be significantly more concise, direct, and free of unnecessary filler words,
       nominalizations, and passive voice, while retaining the original meaning and professional tone.
       
       Only output the revised, concise text.
       
       Do not include any explanation, introduction, or conversation.
       """)
		@UserMessage("""
		This is the text to be rewritten : {{text}}
		""")
		public TokenStream rewrite(@V("text") String text);
	}

	@Reference
	private WorkflowInstanceManager _workflowInstanceManager;

	private static final String _PROJECT = "upgrades-accelerator-liferay";
	private static final String _LOCATION = "us-central1";
	private static final String _MODEL_NAME = "gemini-2.5-flash-lite";
}
