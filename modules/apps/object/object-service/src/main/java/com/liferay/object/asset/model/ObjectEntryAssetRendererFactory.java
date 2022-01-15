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

import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.BaseAssetRendererFactory;
import com.liferay.object.constants.ObjectPortletKeys;
import com.liferay.object.internal.security.permission.resource.ObjectEntryModelResourcePermission;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;

import javax.servlet.ServletContext;

/**
 * @author Feliphe Marinho
 */
public class ObjectEntryAssetRendererFactory extends
	BaseAssetRendererFactory<ObjectEntry> {

	public ObjectEntryAssetRendererFactory(
		String className,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryModelResourcePermission objectEntryModelResourcePermission,
		ObjectEntryLocalService objectEntryLocalService, ServletContext servletContext) {

		setClassName(className);
		setLinkable(true);
		setPortletId(ObjectPortletKeys.OBJECT_ENTRIES);
		setSearchable(true);

		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryModelResourcePermission = objectEntryModelResourcePermission;
		_objectEntryLocalService = objectEntryLocalService;
		_servletContext = servletContext;
	}

	@Override
	public AssetRenderer<ObjectEntry> getAssetRenderer(
		long classPK, int type) throws PortalException {

		ObjectEntry objectEntry = _objectEntryLocalService.getObjectEntry(classPK);
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				objectEntry.getObjectDefinitionId());

		ObjectEntryAssetRenderer objectEntryAssetRenderer =
			new ObjectEntryAssetRenderer(
				objectEntry, objectDefinition,
				_objectEntryModelResourcePermission);

		objectEntryAssetRenderer.setServletContext(_servletContext);

		return objectEntryAssetRenderer;
	}

	@Override
	public String getType() {
		return "object";
	}

	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final ObjectEntryModelResourcePermission _objectEntryModelResourcePermission;
	private final ServletContext _servletContext;
}