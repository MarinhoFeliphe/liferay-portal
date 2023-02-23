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

package com.liferay.notification.web.internal.custom.tab;

import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.portlet.PortletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Feliphe Marinho
 */
public class CustomTabDisplayContext {

	public CustomTabDisplayContext(
		HttpServletRequest httpServletRequest,
		ObjectDefinition objectDefinition1, ObjectDefinition objectDefinition2,
		ObjectRelationshipLocalService objectRelationshipLocalService,
		Portal portal) {

		_httpServletRequest = httpServletRequest;
		_objectDefinition1 = objectDefinition1;
		_objectDefinition2 = objectDefinition2;
		_objectRelationshipLocalService = objectRelationshipLocalService;
		_portal = portal;
	}

	public String getAPIURL(ObjectEntry objectEntry) {
		return StringBundler.concat(
			"/o/c/employees/?filter=r_departmentEmployee_c_departmentId%20eq%2",
			"0%27", objectEntry.getObjectEntryId(), "%27");
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
			ObjectEntry objectEntry)
		throws Exception {

		if ((Boolean)_httpServletRequest.getAttribute("readOnly")) {
			return Collections.emptyList();
		}

		return Arrays.asList(
			new FDSActionDropdownItem(
				_getDeleteURL(_httpServletRequest, objectEntry), "trash",
				"delete", LanguageUtil.get(_httpServletRequest, "delete"),
				"post", "replace", "async"));
	}

	public ObjectRelationship getObjectRelationship() throws PortalException {
		String relationshipName = ParamUtil.getString(
			_httpServletRequest, "screenNavigationCategoryKey");

		return _objectRelationshipLocalService.getObjectRelationship(
			_objectDefinition1.getObjectDefinitionId(), relationshipName);
	}

	private String _getDeleteURL(
		HttpServletRequest httpServletRequest, ObjectEntry objectEntry) {

		return PortletURLBuilder.create(
			_portal.getControlPanelPortletURL(
				httpServletRequest, _objectDefinition1.getPortletId(),
				PortletRequest.ACTION_PHASE)
		).setActionName(
			"/object_entries/edit_object_entry"
		).setCMD(
			"disassociateRelatedModels"
		).setRedirect(
			ParamUtil.getString(
				httpServletRequest, "currentUrl",
				_portal.getCurrentURL(httpServletRequest))
		).setParameter(
			"className", _objectDefinition2.getClassName()
		).setParameter(
			"objectEntryId", objectEntry.getObjectEntryId()
		).setParameter(
			"objectRelationshipId",
			() -> {
				ObjectRelationship objectRelationship = getObjectRelationship();

				return objectRelationship.getObjectRelationshipId();
			}
		).setParameter(
			"relatedModelId", "{id}"
		).buildString();
	}

	private final HttpServletRequest _httpServletRequest;
	private final ObjectDefinition _objectDefinition1;
	private final ObjectDefinition _objectDefinition2;
	private final ObjectRelationshipLocalService
		_objectRelationshipLocalService;
	private final Portal _portal;

}