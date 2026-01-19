/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agent;

import com.liferay.ai.hub.agent.AgentContext;
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
 * @author João Victor Alves
 */
public class FixSpellingAndGrammarAgent implements AgentSpecsProvider {

	public FixSpellingAndGrammarAgent(
		AgentContext agentContext,
		WorkflowInstanceActionExecutor workflowInstanceActionExecutor,
		WorkflowInstanceManager workflowInstanceManager) {

		_agentContext = agentContext;
		_workflowInstanceActionExecutor = workflowInstanceActionExecutor;
		_workflowInstanceManager = workflowInstanceManager;
	}

	@Override
	public boolean async() {
		return false;
	}

	@Override
	public String description() {
		return "This writing agent acts as an expert linguistic editor, " +
			"correcting grammar and spelling errors while strictly " +
				"preserving your original tone and style.";
	}

	@Override
	public String inputKey() {
		return "context";
	}

	@Agent
	public String invoke(Map<String, Object> input) {
		try {
			CompletableFuture<String> completableFuture =
				new CompletableFuture<>();

			WorkflowInstance workflowInstance =
				_workflowInstanceManager.startWorkflowInstance(
					_agentContext.getCompanyId(), _agentContext.getGroupId(),
					_agentContext.getUserId(),
					WorkflowDefinitionConstants.NAME_FIX_SPELLING_AND_GRAMMAR,
					1, null,
					HashMapBuilder.<String, Serializable>put(
						WorkflowConstants.CONTEXT_SERVICE_CONTEXT,
						_agentContext.getServiceContext()
					).put(
						"text", GetterUtil.getString(input.get("text"))
					).build());

			_workflowInstanceActionExecutor.addCompletionAction(
				workflowInstance.getWorkflowInstanceId(),
				workflowContext -> completableFuture.complete(
					GetterUtil.getString(workflowContext.get(outputKey()))));

			return completableFuture.get(10, TimeUnit.SECONDS);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public AgentListener listener() {
		return null;
	}

	@Override
	public String outputKey() {
		return "rewrittenText";
	}

	private final AgentContext _agentContext;
	private final WorkflowInstanceActionExecutor
		_workflowInstanceActionExecutor;
	private final WorkflowInstanceManager _workflowInstanceManager;

}