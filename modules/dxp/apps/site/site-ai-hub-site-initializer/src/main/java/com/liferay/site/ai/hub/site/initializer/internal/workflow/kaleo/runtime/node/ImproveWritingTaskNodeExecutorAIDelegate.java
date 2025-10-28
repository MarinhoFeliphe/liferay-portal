/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.node.TaskNodeExecutorAIDelegate;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(service = TaskNodeExecutorAIDelegate.class)
public class ImproveWritingTaskNodeExecutorAIDelegate
	implements TaskNodeExecutorAIDelegate {

	@Override
	public void execute(
		ExecutionContext executionContext, String taskNodeName) {

		if (!Objects.equals(taskNodeName, getKey())) {
			return;
		}

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		Map<String, Serializable> context =
			(Map<String, Serializable>)workflowContext.get("context");

		VertexAiGeminiStreamingChatModel vertexAiGeminiStreamingChatModel =
			VertexAiGeminiStreamingChatModel.builder(
			).project(
				_PROJECT
			).location(
				_LOCATION
			).modelName(
				_MODEL_NAME
			).build();

		Assistant assistant = AiServices.builder(
			Assistant.class
		).systemMessageProvider(
			object -> StringBundler.concat(
				"You are a professional writing editor. Your sole task is to ",
				"take the provided text and rewrite it to be significantly ",
				"more concise, direct, and free of unnecessary filler words, ",
				"nominalizations, and passive voice, while retaining the ",
				"original meaning and professional tone. Only output the ",
				"revised, concise text. Do not include any explanation, ",
				"introduction, or conversation.")
		).streamingChatModel(
			vertexAiGeminiStreamingChatModel
		).build();

		assistant.rewrite(
			"This is the text to be rewritten : " +
				GetterUtil.getString(context.get("text"))
		).onCompleteResponse(
			response -> _completeResponse(
				response, executionContext, vertexAiGeminiStreamingChatModel)
		).onError(
			throwable -> vertexAiGeminiStreamingChatModel.close()
		).start();
	}

	@Override
	public String getKey() {
		return "improveWriting";
	}

	public interface Assistant {

		public TokenStream rewrite(String text);

	}

	private void _completeResponse(
		ChatResponse chatResponse, ExecutionContext executionContext,
		VertexAiGeminiStreamingChatModel vertexAiGeminiStreamingChatModel) {

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		AiMessage aiMessage = chatResponse.aiMessage();

		workflowContext.put("text", aiMessage.text());

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		try {
			_workflowInstanceManager.updateWorkflowContext(
				kaleoInstanceToken.getCompanyId(),
				kaleoInstanceToken.getKaleoInstanceId(), workflowContext);
		}
		catch (WorkflowException workflowException) {
			throw new RuntimeException(workflowException);
		}
		finally {
			vertexAiGeminiStreamingChatModel.close();
		}
	}

	private static final String _LOCATION = "us-central1";

	private static final String _MODEL_NAME = "gemini-2.5-flash-lite";

	private static final String _PROJECT = "upgrades-accelerator-liferay";

	@Reference
	private WorkflowInstanceManager _workflowInstanceManager;

}