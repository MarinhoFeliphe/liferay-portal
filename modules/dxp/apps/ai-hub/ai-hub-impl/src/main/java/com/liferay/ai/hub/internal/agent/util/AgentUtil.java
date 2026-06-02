/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agent.util;

import com.liferay.ai.hub.internal.audit.constants.AIHubEventTypes;
import com.liferay.ai.hub.internal.constants.AIHubDestinationNames;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowInstance;

import java.io.Serializable;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Feliphe Marinho
 * @author João Victor Alves
 */
public class AgentUtil {

	public static void complete(Message message) {
		CompletableFuture<Map<String, Serializable>> completableFuture =
			_completableFutures.remove(message.getLong("workflowInstanceId"));

		if (completableFuture != null) {
			completableFuture.complete(
				(Map<String, Serializable>)message.get("workflowContext"));
		}

		message.put(
			"eventType", AIHubEventTypes.AI_HUB_AGENT_INSTANCE_COMPLETE);
		message.put("timestamp", new Date());

		MessageBusUtil.sendMessage(
			AIHubDestinationNames.AI_HUB_AGENT_INSTANCE, message);
	}

	public static void completeExceptionally(Message message) {
		CompletableFuture<Map<String, Serializable>> completableFuture =
			_completableFutures.remove(message.getLong("workflowInstanceId"));

		if (completableFuture != null) {
			completableFuture.complete(
				HashMapBuilder.<String, Serializable>put(
					"exception", (Exception)message.get("exception")
				).build());
		}

		message.put(
			"eventType", AIHubEventTypes.AI_HUB_AGENT_INSTANCE_COMPLETE);
		message.put("timestamp", new Date());

		MessageBusUtil.sendMessage(
			AIHubDestinationNames.AI_HUB_AGENT_INSTANCE, message);
	}

	public static String getOutput(
			long userId, WorkflowInstance workflowInstance)
		throws Exception {

		Message message = new Message();

		message.put("eventType", AIHubEventTypes.AI_HUB_AGENT_INSTANCE_START);
		message.put("timestamp", new Date());
		message.put("userId", userId);
		message.put(
			"workflowInstanceId", workflowInstance.getWorkflowInstanceId());

		MessageBusUtil.sendMessage(
			AIHubDestinationNames.AI_HUB_AGENT_INSTANCE, message);

		CompletableFuture<Map<String, Serializable>> completableFuture =
			new CompletableFuture<>();

		_completableFutures.put(
			workflowInstance.getWorkflowInstanceId(), completableFuture);

		Map<String, Serializable> workflowContext = completableFuture.get(
			1, TimeUnit.MINUTES);

		if (workflowContext.containsKey("exception")) {
			throw (Exception)workflowContext.get("exception");
		}

		return MapUtil.getString(workflowContext, "output");
	}

	private static final ConcurrentMap
		<Long, CompletableFuture<Map<String, Serializable>>>
			_completableFutures = new ConcurrentHashMap<>();

}