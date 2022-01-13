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
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @author Feliphe Marinho
 */
public class ObjectEntryAssetRenderer extends
	BaseJSPAssetRenderer<ObjectEntry> {

	public ObjectEntryAssetRenderer(
		long classPK, ObjectEntryService objectEntryService)
		throws PortalException {
		_objectEntry = objectEntryService.getObjectEntry(
			classPK);
		_objectEntryService = objectEntryService;
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
		return "/asset/full_content.jsp";
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
	public String getURLViewInContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		String noSuchEntryRedirect)
		throws Exception {

		return null;
	}

	@Override
	public boolean hasEditPermission(PermissionChecker permissionChecker)
		throws PortalException {
		return _objectEntryService.hasModelResourcePermission(
			_objectEntry, ActionKeys.UPDATE);
	}

	@Override
	public boolean hasViewPermission(PermissionChecker permissionChecker)
		throws PortalException {
		return _objectEntryService.hasModelResourcePermission(
			_objectEntry, ActionKeys.VIEW);
	}

	private final ObjectEntry _objectEntry;
	private final ObjectEntryService _objectEntryService;
}
