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
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.internal.AgentSpecsProvider;
import dev.langchain4j.agentic.observability.AgentListener;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Feliphe Marinho
 */
public class MakeLongerAgent implements AgentSpecsProvider {

	private final long _companyId = 0L;
	private final long _groupId = 0L;
	private final long _userId = 0L;
	private final WorkflowInstanceManager _workflowInstanceManager;
	private final WorkflowInstanceActionExecutor _workflowInstanceActionExecutor;

	public MakeLongerAgent(
		AgentContext agentContext,
		WorkflowInstanceActionExecutor workflowInstanceActionExecutor,
		WorkflowInstanceManager workflowInstanceManager) {

		_workflowInstanceActionExecutor = workflowInstanceActionExecutor;
		_workflowInstanceManager = workflowInstanceManager;
	}

	@Agent
	public String invoke(Map<String, Object> input) {
		try {
			CompletableFuture<String> completableFuture =
				new CompletableFuture<>();

			WorkflowInstance workflowInstance =
				_workflowInstanceManager.startWorkflowInstance(
					_companyId, _groupId, _userId,
					WorkflowDefinitionConstants.NAME_MAKE_LONGER, 1, null,
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
		return "text";
	}

	@Override
	public String outputKey() {
		return "rewrittenText";
	}

	@Override
	public String description() {
		return "This writing agent takes a given text and expands it " +
			   "into a longer, more detailed version.";
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
