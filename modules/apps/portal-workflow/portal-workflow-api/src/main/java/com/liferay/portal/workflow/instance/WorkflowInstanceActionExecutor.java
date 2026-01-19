/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.instance;

import java.io.Serializable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Feliphe Marinho
 */
public interface WorkflowInstanceActionExecutor {

	public void addCompletionAction(
		long workflowInstanceId, Consumer<Map<String, Serializable>> consumer);

	public void executeCompletionAction(
		long workflowInstanceId, Map<String, Serializable> workflowContext);

}