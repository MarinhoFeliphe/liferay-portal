/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.runtime.integration.internal;

import com.liferay.portal.workflow.instance.WorkflowInstanceActionExecutor;

import java.io.Serializable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;

/**
 * @author Feliphe Marinho
 */
@Component(service = WorkflowInstanceActionExecutor.class)
public class WorkflowInstanceActionExecutorImpl
	implements WorkflowInstanceActionExecutor {

	@Override
	public void addCompletionAction(
		long workflowInstanceId, Consumer<Map<String, Serializable>> consumer) {

		_consumers.put(workflowInstanceId, consumer);
	}

	@Override
	public void executeCompletionAction(
		long workflowInstanceId, Map<String, Serializable> workflowContext) {

		if (_consumers.isEmpty()) {
			return;
		}

		Consumer<Map<String, Serializable>> consumer = _consumers.remove(
			workflowInstanceId);

		if (consumer == null) {
			return;
		}

		consumer.accept(workflowContext);
	}

	private final Map<Long, Consumer<Map<String, Serializable>>> _consumers =
		new ConcurrentHashMap<>();

}