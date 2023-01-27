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

package com.liferay.notification.context;

import com.liferay.notification.model.NotificationTemplate;

import java.util.Map;

/**
 * @author Feliphe Marinho
 */
public class NotificationContext {

	public String getClassName() {
		return _className;
	}

	public long getClassPK() {
		return _classPK;
	}

	public String getExternalReferenceCode() {
		return _externalReferenceCode;
	}

	public NotificationTemplate getNotificationTemplate() {
		return _notificationTemplate;
	}

	public String getPortletId() {
		return _portletId;
	}

	public Map<String, Object> getTermValues() {
		return _termValues;
	}

	public long getUserId() {
		return _userId;
	}

	public void setClassName(String className) {
		_className = className;
	}

	public void setClassPK(long classPK) {
		_classPK = classPK;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		_externalReferenceCode = externalReferenceCode;
	}

	public void setNotificationTemplate(
		NotificationTemplate notificationTemplate) {

		_notificationTemplate = notificationTemplate;
	}

	public void setPortletId(String portletId) {
		_portletId = portletId;
	}

	public void setTermValues(Map<String, Object> termValues) {
		_termValues = termValues;
	}

	public void setUserId(long userId) {
		_userId = userId;
	}

	private String _className;
	private long _classPK;
	private String _externalReferenceCode;
	private NotificationTemplate _notificationTemplate;
	private String _portletId;
	private Map<String, Object> _termValues;
	private long _userId;

}