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

package com.liferay.portal.workflow.taglib.internal.display.context;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.workflow.taglib.internal.constants.WorkflowStatusTaglibConstants;
import com.liferay.portal.workflow.taglib.internal.constants.WorkflowStatusTaglibWebKeys;

import java.util.Objects;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Feliphe Marinho
 */
public class WorkflowStatusDisplayContext {

	public String getHelpMessage(HttpServletRequest httpServletRequest) {
		return GetterUtil.getString(
			_getNamespacedAttribute(
				httpServletRequest, WorkflowStatusTaglibWebKeys.HELP_MESSAGE));
	}

	public String getId(HttpServletRequest httpServletRequest) {
		return GetterUtil.getString(
			_getNamespacedAttribute(
				httpServletRequest, WorkflowStatusTaglibWebKeys.ID));
	}

	public String getInstanceStatus(
		HttpServletRequest httpServletRequest, ResourceBundle resourceBundle) {

		Object bean = _getBean(httpServletRequest);
		Class<?> model = _getModel(httpServletRequest);

		if (Objects.equals(
				getStatus(httpServletRequest),
				WorkflowConstants.STATUS_PENDING) &&
			(bean != null) && (model != null) &&
			_isShowInstanceStatus(httpServletRequest)) {

			try {
				String workflowStatus =
					WorkflowInstanceLinkLocalServiceUtil.getState(
						BeanPropertiesUtil.getLong(bean, "companyId"),
						BeanPropertiesUtil.getLong(bean, "groupId"),
						model.getName(),
						BeanPropertiesUtil.getLong(bean, "primaryKey"));

				return StringBundler.concat(
					StringPool.SPACE, StringPool.OPEN_PARENTHESIS,
					HtmlUtil.escape(
						LanguageUtil.get(resourceBundle, workflowStatus)),
					StringPool.CLOSE_PARENTHESIS);
			}
			catch (PortalException portalException) {
				if (_log.isDebugEnabled()) {
					_log.debug(portalException.getMessage(), portalException);
				}
			}
		}

		return StringPool.BLANK;
	}

	public Integer getStatus(HttpServletRequest httpServletRequest) {
		Object bean = _getBean(httpServletRequest);

		if (bean != null) {
			return BeanPropertiesUtil.getInteger(bean, "status");
		}

		return GetterUtil.getInteger(
			_getNamespacedAttribute(
				httpServletRequest, WorkflowStatusTaglibWebKeys.STATUS));
	}

	public String getStatusMessage(HttpServletRequest httpServletRequest) {
		if (Validator.isNotNull(
				GetterUtil.getString(
					_getNamespacedAttribute(
						httpServletRequest,
						WorkflowStatusTaglibWebKeys.STATUS_MESSAGE)))) {

			return GetterUtil.getString(
				_getNamespacedAttribute(
					httpServletRequest,
					WorkflowStatusTaglibWebKeys.STATUS_MESSAGE));
		}

		return WorkflowConstants.getStatusLabel(getStatus(httpServletRequest));
	}

	public String getVersion(HttpServletRequest httpServletRequest) {
		return GetterUtil.getString(
			_getNamespacedAttribute(
				httpServletRequest, WorkflowStatusTaglibWebKeys.VERSION));
	}

	public boolean isShowHelpMessage(HttpServletRequest httpServletRequest) {
		return GetterUtil.getBoolean(
			_getNamespacedAttribute(
				httpServletRequest,
				WorkflowStatusTaglibWebKeys.SHOW_HELP_MESSAGE),
			true);
	}

	public boolean isShowLabel(HttpServletRequest httpServletRequest) {
		return GetterUtil.getBoolean(
			_getNamespacedAttribute(
				httpServletRequest, WorkflowStatusTaglibWebKeys.SHOW_LABEL),
			true);
	}

	private Object _getBean(HttpServletRequest httpServletRequest) {
		return _getNamespacedAttribute(
			httpServletRequest, WorkflowStatusTaglibWebKeys.BEAN);
	}

	private Class<?> _getModel(HttpServletRequest httpServletRequest) {
		return (Class<?>)_getNamespacedAttribute(
			httpServletRequest, WorkflowStatusTaglibWebKeys.MODEL);
	}

	private Object _getNamespacedAttribute(
		HttpServletRequest httpServletRequest, String attributeName) {

		return httpServletRequest.getAttribute(
			WorkflowStatusTaglibConstants.ATTRIBUTE_NAMESPACE + attributeName);
	}

	private boolean _isShowInstanceStatus(
		HttpServletRequest httpServletRequest) {

		return GetterUtil.getBoolean(
			_getNamespacedAttribute(
				httpServletRequest,
				WorkflowStatusTaglibWebKeys.SHOW_INSTANCE_STATUS));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		WorkflowStatusDisplayContext.class);

}