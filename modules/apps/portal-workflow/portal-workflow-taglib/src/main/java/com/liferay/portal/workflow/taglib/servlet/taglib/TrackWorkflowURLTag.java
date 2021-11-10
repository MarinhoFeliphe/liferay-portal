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

package com.liferay.portal.workflow.taglib.servlet.taglib;

import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.workflow.constants.WorkflowPortletKeys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Feliphe Marinho
 */
public class TrackWorkflowURLTag extends TagSupport {

	public static String doTag(
			Object bean, Class<?> model, HttpServletRequest httpServletRequest)
		throws Exception {

		return PortletURLBuilder.create(
			PortletProviderUtil.getPortletURL(
				httpServletRequest, WorkflowPortletKeys.INSTANCE_TRACKER,
				PortletProvider.Action.VIEW)
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
		TrackWorkflowURLTag.class);

}