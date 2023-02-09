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

package com.liferay.notification.web.internal.foo.folder;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.criteria.InfoItemItemSelectorReturnType;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.scope.ObjectScopeProvider;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Feliphe Marinho
 */
public class FooDisplayContext {

	public FooDisplayContext(
		HttpServletRequest httpServletRequest, ItemSelector itemSelector,
		ObjectEntryManagerRegistry objectEntryManagerRegistry,
		ObjectDefinition objectDefinition2,
		ObjectRelationshipLocalService objectRelationshipLocalService,
		ObjectScopeProviderRegistry objectScopeProviderRegistry) {

		_itemSelector = itemSelector;
		_objectEntryManagerRegistry = objectEntryManagerRegistry;
		_objectDefinition2 = objectDefinition2;
		_objectRelationshipLocalService = objectRelationshipLocalService;
		_objectScopeProviderRegistry = objectScopeProviderRegistry;

		_fooRequestHelper = new FooRequestHelper(httpServletRequest);
		_objectDefinition1 = _getObjectDefinition();
		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() {
		return "/o/c/authors";
	}

	public CreationMenu getCreationMenu() {
		CreationMenu creationMenu = new CreationMenu();

		LiferayPortletResponse liferayPortletResponse =
			_fooRequestHelper.getLiferayPortletResponse();

		creationMenu.addDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					liferayPortletResponse.getNamespace() +
						"selectRelatedModel");
				dropdownItem.setLabel(
					LanguageUtil.get(
						_fooRequestHelper.getRequest(), "select-existing-one"));
				dropdownItem.setTarget("event");
			});

		return creationMenu;
	}

	public long getObjectEntryId() {
		String externalReferenceCode = ParamUtil.getString(
			_fooRequestHelper.getRequest(), "externalReferenceCode");

		if (Validator.isNull(externalReferenceCode)) {
			HttpServletRequest httpServletRequest =
				_fooRequestHelper.getRequest();

			externalReferenceCode = (String)httpServletRequest.getAttribute(
				"EXTERNAL_REFERENCE_CODE");
		}

		ObjectDefinition objectDefinition = _getObjectDefinition();

		ObjectEntry objectEntry = null;

		ObjectEntryManager objectEntryManager =
			_objectEntryManagerRegistry.getObjectEntryManager(
				objectDefinition.getStorageType());

		try {
			objectEntry = objectEntryManager.getObjectEntry(
				_getDTOConverterContext(), externalReferenceCode,
				_fooRequestHelper.getCompanyId(), objectDefinition,
				String.valueOf(_getGroupId()));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}
		}

		return objectEntry.getId();
	}

	public ObjectRelationship getObjectRelationship() throws PortalException {
		String relationshipName = ParamUtil.getString(
			_fooRequestHelper.getRequest(), "screenNavigationCategoryKey");

		ObjectDefinition objectDefinition = _getObjectDefinition();

		return _objectRelationshipLocalService.getObjectRelationship(
			objectDefinition.getObjectDefinitionId(), relationshipName);
	}

	public String getRelatedObjectEntryItemSelectorURL()
		throws PortalException {

		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			RequestBackedPortletURLFactoryUtil.create(
				_fooRequestHelper.getRequest());

		LiferayPortletResponse liferayPortletResponse =
			_fooRequestHelper.getLiferayPortletResponse();

		InfoItemItemSelectorCriterion infoItemItemSelectorCriterion =
			new InfoItemItemSelectorCriterion();

		infoItemItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			Collections.singletonList(new InfoItemItemSelectorReturnType()));

		infoItemItemSelectorCriterion.setItemType(
			_objectDefinition2.getClassName());

		ObjectRelationship objectRelationship = getObjectRelationship();

		return PortletURLBuilder.create(
			_itemSelector.getItemSelectorURL(
				requestBackedPortletURLFactory,
				liferayPortletResponse.getNamespace() +
					"selectRelatedModalEntry",
				infoItemItemSelectorCriterion)
		).setParameter(
			"groupId", _getGroupId()
		).setParameter(
			"objectDefinitionId", _objectDefinition1::getObjectDefinitionId
		).setParameter(
			"objectEntryId", getObjectEntryId()
		).setParameter(
			"objectRelationshipId", objectRelationship.getObjectRelationshipId()
		).setParameter(
			"objectRelationshipType", objectRelationship.getType()
		).buildString();
	}

	public boolean isDefaultUser() {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (permissionChecker == null) {
			return true;
		}

		User user = permissionChecker.getUser();

		return user.isDefaultUser();
	}

	private DTOConverterContext _getDTOConverterContext() {
		return new DefaultDTOConverterContext(
			false, null, null, _fooRequestHelper.getRequest(), null,
			_themeDisplay.getLocale(), null, _themeDisplay.getUser());
	}

	private long _getGroupId() {
		ObjectScopeProvider objectScopeProvider =
			_objectScopeProviderRegistry.getObjectScopeProvider(
				_objectDefinition1.getScope());

		try {
			return objectScopeProvider.getGroupId(
				_fooRequestHelper.getRequest());
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			return 0L;
		}
	}

	private ObjectDefinition _getObjectDefinition() {
		HttpServletRequest httpServletRequest = _fooRequestHelper.getRequest();

		return (ObjectDefinition)httpServletRequest.getAttribute(
			"OBJECT_DEFINITION");
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FooDisplayContext.class);

	private final FooRequestHelper _fooRequestHelper;
	private final ItemSelector _itemSelector;
	private final ObjectDefinition _objectDefinition1;
	private final ObjectDefinition _objectDefinition2;
	private final ObjectEntryManagerRegistry _objectEntryManagerRegistry;
	private final ObjectRelationshipLocalService
		_objectRelationshipLocalService;
	private final ObjectScopeProviderRegistry _objectScopeProviderRegistry;
	private final ThemeDisplay _themeDisplay;

}