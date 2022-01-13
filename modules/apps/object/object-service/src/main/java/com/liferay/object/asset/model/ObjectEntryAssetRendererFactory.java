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
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.portal.kernel.exception.PortalException;

import javax.servlet.ServletContext;

/**
 * @author Feliphe Marinho
 */
public class ObjectEntryAssetRendererFactory extends
	BaseAssetRendererFactory<ObjectEntry> {

	public ObjectEntryAssetRendererFactory(
		String className, ObjectEntryService objectEntryService,
		ServletContext servletContext) {

		setClassName(className);
		setLinkable(true);
		setPortletId(ObjectPortletKeys.OBJECT_ENTRIES);
		setSearchable(true);

		_objectEntryService = objectEntryService;
		_servletContext = servletContext;
	}

	@Override
	public AssetRenderer<ObjectEntry> getAssetRenderer(
		long classPK, int type) throws PortalException {

		ObjectEntry objectEntry = _objectEntryService.getObjectEntry(
			classPK);

		ObjectEntryAssetRenderer objectEntryAssetRenderer =
			new ObjectEntryAssetRenderer(classPK, _objectEntryService);

		objectEntryAssetRenderer.setServletContext(_servletContext);

		return objectEntryAssetRenderer;
	}

	@Override
	public String getType() {
		return "object";
	}

	private final ObjectEntryService _objectEntryService;

	private final ServletContext _servletContext;
}
