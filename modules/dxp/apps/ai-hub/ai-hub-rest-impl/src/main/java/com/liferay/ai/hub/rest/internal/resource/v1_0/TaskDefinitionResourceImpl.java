/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.rest.internal.resource.v1_0;

import com.liferay.ai.hub.rest.dto.v1_0.TaskDefinition;
import com.liferay.ai.hub.rest.resource.v1_0.TaskDefinitionResource;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.workflow.comparator.WorkflowComparatorFactory;
import com.liferay.portal.workflow.manager.WorkflowDefinitionManager;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Feliphe Marinho
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/task-definition.properties",
	scope = ServiceScope.PROTOTYPE, service = TaskDefinitionResource.class
)
public class TaskDefinitionResourceImpl extends BaseTaskDefinitionResourceImpl {

	@Override
	public Page<TaskDefinition> getTaskDefinitionsPage(
			String search, Filter filter, Pagination pagination, Sort[] sorts)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-62272")) {

			throw new UnsupportedOperationException();
		}

		return Page.of(
			HashMapBuilder.put(
				"get",
				addAction(
					ActionKeys.VIEW, "getTaskDefinitionsPage",
					WorkflowConstants.RESOURCE_NAME, null)
			).build(),
			transform(
				_workflowDefinitionManager.getLatestKaleoDefinitionVersions(
					contextCompany.getCompanyId(), "ai", null,
					WorkflowConstants.STATUS_ANY, LocaleUtil.CANADA,
					pagination.getStartPosition(), pagination.getEndPosition(),
					_toOrderByComparator((Sort)ArrayUtil.getValue(sorts, 0))),
				this::_toTaskDefinition),
			pagination,
			_workflowDefinitionManager.getLatestWorkflowDefinitionsCount(
				contextCompany.getCompanyId()));
	}

	private OrderByComparator<WorkflowDefinition> _toOrderByComparator(
		Sort sort) {

		if (sort == null) {
			return _workflowComparatorFactory.
				getDefinitionModifiedDateComparator(false);
		}

		if (StringUtil.equals(sort.getFieldName(), "name")) {
			return _workflowComparatorFactory.getDefinitionNameComparator(
				!sort.isReverse());
		}

		return _workflowComparatorFactory.getDefinitionModifiedDateComparator(
			!sort.isReverse());
	}

	private TaskDefinition _toTaskDefinition(
			WorkflowDefinition workflowDefinition)
		throws PortalException {

		return new TaskDefinition() {
			{
				setDescription(workflowDefinition::getDescription);
				setName(workflowDefinition::getName);
				setVersion(workflowDefinition::getVersion);
			}
		};
	}

	@Reference
	private WorkflowComparatorFactory _workflowComparatorFactory;

	@Reference
	private WorkflowDefinitionManager _workflowDefinitionManager;

}