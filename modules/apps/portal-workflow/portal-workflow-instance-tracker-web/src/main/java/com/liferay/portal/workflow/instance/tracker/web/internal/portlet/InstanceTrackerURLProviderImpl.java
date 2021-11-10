/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.instance.tracker.web.internal.portlet;

import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.workflow.constants.WorkflowPortletKeys;
import com.liferay.portal.workflow.instance.tracker.util.InstanceTrackerURLProvider;
import org.osgi.service.component.annotations.Component;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Feliphe Marinho
 */
@Component(immediate = true, service = InstanceTrackerURLProvider.class)
public class InstanceTrackerURLProviderImpl implements
	InstanceTrackerURLProvider {

	@Override
	public String getURL(
		Object bean, Class<?> model, HttpServletRequest httpServletRequest) {

		return PortletURLBuilder.create(
			PortalUtil.getControlPanelPortletURL(
				httpServletRequest, null, WorkflowPortletKeys.INSTANCE_TRACKER, 0, 0,
				PortletRequest.RENDER_PHASE)
		).setParameter(
			"instanceId",
			() -> {
				try {
					WorkflowInstanceLink workflowInstanceLink =
						WorkflowInstanceLinkLocalServiceUtil.
							getWorkflowInstanceLink(
								BeanPropertiesUtil.getLong(bean, "companyId"),
								BeanPropertiesUtil.getLong(bean, "groupId"),
								model.getName(),
								BeanPropertiesUtil.getLong(bean, "primaryKey"));

					return workflowInstanceLink.getWorkflowInstanceId();
				}
				catch (PortalException portalException) {
					if (_log.isDebugEnabled()) {
						_log.debug(
							portalException.getMessage(), portalException);
					}
				}

				return null;
			}
		).setWindowState(
			LiferayWindowState.POP_UP
		).buildString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		InstanceTrackerURLProviderImpl.class);
}
