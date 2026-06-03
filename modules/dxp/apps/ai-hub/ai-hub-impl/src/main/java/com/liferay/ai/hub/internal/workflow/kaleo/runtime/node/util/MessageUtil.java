/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util;

import com.liferay.ai.hub.internal.audit.constants.AIHubEventTypes;
import com.liferay.ai.hub.internal.constants.AIHubDestinationNames;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.output.TokenUsage;

import java.io.Serializable;

import java.util.Date;

/**
 * @author João Victor Alves
 */
public class MessageUtil {

	public static void sendMessage(
		ChatResponse chatResponse, KaleoInstanceToken kaleoInstanceToken,
		String prompt, ServiceContext serviceContext, String userMessage) {

		ChatResponseMetadata chatResponseMetadata = chatResponse.metadata();

		TokenUsage tokenUsage = chatResponseMetadata.tokenUsage();

		int inputTokenCount = tokenUsage.inputTokenCount();
		int outputTokenCount = tokenUsage.outputTokenCount();
		int totalTokenCount = tokenUsage.totalTokenCount();

		int thoughtsTokenCount =
			totalTokenCount - inputTokenCount - outputTokenCount;

		Message message = new Message();

		message.put(
			"eventType",
			AIHubEventTypes.AI_HUB_AGENT_INSTANCE_PARTIALLY_COMPLETE);
		message.put("kaleoInstanceToken", kaleoInstanceToken);
		message.put(
			"metadata",
			HashMapBuilder.<String, Serializable>put(
				"inputTokenCount", String.valueOf(inputTokenCount)
			).put(
				"outputTokenCount", String.valueOf(outputTokenCount)
			).put(
				"promptInput", prompt
			).put(
				"thoughtsTokenCount", String.valueOf(thoughtsTokenCount)
			).put(
				"totalTokenCount", String.valueOf(totalTokenCount)
			).put(
				"userMessageInput", userMessage
			).build());
		message.put("serviceContext", serviceContext);
		message.put("timestamp", new Date());
		message.put("userId", serviceContext.getUserId());
		message.put(
			"workflowInstanceId", kaleoInstanceToken.getKaleoInstanceId());

		MessageBusUtil.sendMessage(
			AIHubDestinationNames.AI_HUB_AGENT_INSTANCE, message);
	}

}