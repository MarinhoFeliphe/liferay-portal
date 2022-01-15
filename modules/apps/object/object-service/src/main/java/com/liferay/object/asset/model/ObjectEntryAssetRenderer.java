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

package com.liferay.object.asset.model;

import com.liferay.asset.kernel.model.BaseJSPAssetRenderer;
import com.liferay.object.internal.security.permission.resource.ObjectEntryModelResourcePermission;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * @author Feliphe Marinho
 */
public class ObjectEntryAssetRenderer extends
	BaseJSPAssetRenderer<ObjectEntry> {

	public ObjectEntryAssetRenderer(
		ObjectEntry objectEntry, ObjectDefinition objectDefinition,
		ObjectEntryModelResourcePermission objectEntryModelResourcePermission)
		throws PortalException {

		_objectEntry = objectEntry;
		_objectDefinition = objectDefinition;
		_objectEntryModelResourcePermission = objectEntryModelResourcePermission;
	}

	@Override
	public ObjectEntry getAssetObject() {
		return _objectEntry;
	}

	@Override
	public long getGroupId() {
		return _objectEntry.getGroupId();
	}

	@Override
	public long getUserId() {
		return _objectEntry.getUserId();
	}

	@Override
	public String getUserName() {
		return _objectEntry.getUserName();
	}

	@Override
	public String getUuid() {
		return _objectEntry.getUuid();
	}

	@Override
	public String getJspPath(
		HttpServletRequest httpServletRequest, String template) {

		if (template.equals(TEMPLATE_ABSTRACT) ||
			template.equals(TEMPLATE_FULL_CONTENT)) {
			return "/asset/full_content.jsp";
		}

		return null;
	}

	@Override
	public String getClassName() {
		return _objectEntry.getModelClassName();
	}

	@Override
	public long getClassPK() {
		return _objectEntry.getObjectEntryId();
	}

	@Override
	public String getSummary(
		PortletRequest portletRequest, PortletResponse portletResponse) {
		return StringPool.BLANK;
	}

	@Override
	public String getTitle(Locale locale) {
		return "Title";
	}

	@Override
	public boolean hasEditPermission(PermissionChecker permissionChecker)
		throws PortalException {
		return _objectEntryModelResourcePermission.contains(
			permissionChecker, _objectEntry, ActionKeys.UPDATE);
	}

	@Override
	public boolean hasViewPermission(PermissionChecker permissionChecker)
		throws PortalException {
		return _objectEntryModelResourcePermission.contains(
			permissionChecker, _objectEntry, ActionKeys.VIEW);
	}

	@Override
	public boolean include(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, String template)
		throws Exception {

		httpServletRequest.setAttribute("OBJECT_DEFINITION", _objectDefinition);
		httpServletRequest.setAttribute(
			"objectEntryId", _objectEntry.getObjectEntryId());

		return super.include(httpServletRequest, httpServletResponse, template);
	}

	private final ObjectDefinition _objectDefinition;
	private final ObjectEntry _objectEntry;
	private final ObjectEntryModelResourcePermission _objectEntryModelResourcePermission;
}