/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.rest.internal.resource.v1_0;

import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatus;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatusRegistry;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.workflow.metrics.rest.dto.v1_0.ReindexStatus;
import com.liferay.portal.workflow.metrics.rest.resource.v1_0.ReindexStatusResource;
import com.liferay.portal.workflow.metrics.search.background.task.WorkflowMetricsBackgroundTaskExecutorNames;

import com.liferay.portal.workflow.metrics.search.index.WorkflowMetricsIndex;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.Collections;

/**
 * @author Rafael Praxedes
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/reindex-status.properties",
	scope = ServiceScope.PROTOTYPE, service = ReindexStatusResource.class
)
public class ReindexStatusResourceImpl extends BaseReindexStatusResourceImpl {

	@Override
	public Page<ReindexStatus> getReindexStatusesPage() throws Exception {
		long companyId = contextCompany.getCompanyId();

		if (!_processWorkflowMetricsIndex.exists(companyId) ||
			!_instanceWorkflowMetricsIndex.exists(companyId) ||
			!_slaInstanceResultWorkflowMetricsIndex.exists(companyId) ||
			!_nodeWorkflowMetricsIndex.exists(companyId) ||
			!_taskWorkflowMetricsIndex.exists(companyId) ||
			!_slaTaskResultWorkflowMetricsIndex.exists(companyId) ||
			!_transitionWorkflowMetricsIndex.exists(companyId)) {

			return Page.of(Collections.emptyList());
		}

		return Page.of(
			transform(
				_backgroundTaskLocalService.getBackgroundTasks(
					WorkflowMetricsBackgroundTaskExecutorNames.
						WORKFLOW_METRICS_REINDEX_BACKGROUND_TASK_EXECUTOR,
					BackgroundTaskConstants.STATUS_IN_PROGRESS),
				this::_toReindexStatus));
	}

	@Reference(target = "(workflow.metrics.index.entity.name=instance)")
	private WorkflowMetricsIndex _instanceWorkflowMetricsIndex;

	@Reference(target = "(workflow.metrics.index.entity.name=process)")
	private WorkflowMetricsIndex _processWorkflowMetricsIndex;

	@Reference(
		target = "(workflow.metrics.index.entity.name=sla-instance-result)"
	)
	private WorkflowMetricsIndex _slaInstanceResultWorkflowMetricsIndex;

	@Reference(target = "(workflow.metrics.index.entity.name=node)")
	private WorkflowMetricsIndex _nodeWorkflowMetricsIndex;

	@Reference(target = "(workflow.metrics.index.entity.name=sla-task-result)")
	private WorkflowMetricsIndex _slaTaskResultWorkflowMetricsIndex;

	@Reference(target = "(workflow.metrics.index.entity.name=task)")
	private WorkflowMetricsIndex _taskWorkflowMetricsIndex;

	@Reference(target = "(workflow.metrics.index.entity.name=transition)")
	private WorkflowMetricsIndex _transitionWorkflowMetricsIndex;

	private ReindexStatus _toReindexStatus(BackgroundTask backgroundTask) {
		BackgroundTaskStatus backgroundTaskStatus =
			_backgroundTaskStatusRegistry.getBackgroundTaskStatus(
				backgroundTask.getBackgroundTaskId());

		return new ReindexStatus() {
			{
				completionPercentage = MapUtil.getLong(
					backgroundTaskStatus.getAttributes(), "percentage");
				key = MapUtil.getString(
					backgroundTask.getTaskContextMap(),
					"workflow.metrics.index.key");
			}
		};
	}

	@Reference
	private BackgroundTaskLocalService _backgroundTaskLocalService;

	@Reference
	private BackgroundTaskStatusRegistry _backgroundTaskStatusRegistry;

}