/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.internal.search.index.reindexer.messaging;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.DestinationConfiguration;
import com.liferay.portal.kernel.messaging.DestinationFactory;
import com.liferay.portal.kernel.messaging.DestinationNames;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.scheduler.SchedulerEngineHelper;
import com.liferay.portal.kernel.scheduler.StorageType;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.workflow.metrics.internal.search.index.creation.helper.WorkflowMetricsIndexCreator;
import com.liferay.portal.workflow.metrics.search.index.WorkflowMetricsIndex;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(
	property = "destination.name=" + DestinationNames.WORKFLOW_METRICS_REINDEX,
	service = MessageListener.class
)
public class WorkflowMetricsReindexMessageListener extends BaseMessageListener {

	@Activate
	protected void activate(BundleContext bundleContext) {
		DestinationConfiguration destinationConfiguration =
			new DestinationConfiguration(
				DestinationConfiguration.DESTINATION_TYPE_SYNCHRONOUS,
				DestinationNames.WORKFLOW_METRICS_REINDEX);

		Destination destination = _destinationFactory.createDestination(
			destinationConfiguration);

		_serviceRegistration = bundleContext.registerService(
			Destination.class, destination,
			MapUtil.singletonDictionary(
				"destination.name", destination.getName()));
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();
	}

	@Override
	protected void doReceive(Message message) throws Exception {
		long companyId = message.getLong("companyId");

		if (!_processWorkflowMetricsIndex.exists(companyId) ||
			!_instanceWorkflowMetricsIndex.exists(companyId) ||
			!_slaInstanceResultWorkflowMetricsIndex.exists(companyId) ||
			!_nodeWorkflowMetricsIndex.exists(companyId) ||
			!_taskWorkflowMetricsIndex.exists(companyId) ||
			!_slaTaskResultWorkflowMetricsIndex.exists(companyId) ||
			!_transitionWorkflowMetricsIndex.exists(companyId)) {

			return;
		}

		_workflowMetricsIndexCreator.reindex(
			_companyLocalService.getCompany(companyId));

		_schedulerEngineHelper.delete(
			StringBundler.concat(
				DestinationNames.WORKFLOW_METRICS_REINDEX, StringPool.SLASH,
				companyId),
			StorageType.MEMORY);
	}

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private DestinationFactory _destinationFactory;

	@Reference
	private SchedulerEngineHelper _schedulerEngineHelper;

	private ServiceRegistration<Destination> _serviceRegistration;

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

	@Reference
	private WorkflowMetricsIndexCreator _workflowMetricsIndexCreator;

}