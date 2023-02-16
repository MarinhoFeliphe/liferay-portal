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
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.criteria.InfoItemItemSelectorReturnType;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.object.constants.ObjectActionKeys;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectFieldSettingConstants;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.field.setting.util.ObjectFieldSettingUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.scope.ObjectScopeProvider;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectEntryServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.string.StringBundler;
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
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Feliphe Marinho
 */
public class CustomTabDisplayContext {

	public CustomTabDisplayContext(
		HttpServletRequest httpServletRequest, ItemSelector itemSelector,
		ObjectEntryManagerRegistry objectEntryManagerRegistry,
		ObjectDefinition objectDefinition2,
		ObjectRelationshipLocalService objectRelationshipLocalService,
		ObjectScopeProviderRegistry objectScopeProviderRegistry,
		Portal portal) {

		_customTabRequestHelper = new CustomTabRequestHelper(
			httpServletRequest);

		_itemSelector = itemSelector;
		_objectEntryManagerRegistry = objectEntryManagerRegistry;
		_objectDefinition2 = objectDefinition2;
		_objectRelationshipLocalService = objectRelationshipLocalService;
		_objectScopeProviderRegistry = objectScopeProviderRegistry;
		_portal = portal;

		_objectDefinition1 = _getObjectDefinition();
		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() {
		return StringBundler.concat(
			"/o/c/employees/?filter=r_departmentEmployee_c_departmentId%20eq%2",
			"0%27", getObjectEntryId(), "%27");
	}

	public CreationMenu getCreationMenu() throws PortalException {
		CreationMenu creationMenu = new CreationMenu();

		ObjectRelationship objectRelationship = getObjectRelationship();

		ObjectScopeProvider objectScopeProvider =
			_objectScopeProviderRegistry.getObjectScopeProvider(
				_objectDefinition2.getScope());

		if (!_objectDefinition1.isSystem() && !_objectDefinition2.isSystem() &&
			ObjectEntryServiceUtil.hasPortletResourcePermission(
				objectScopeProvider.getGroupId(
					_customTabRequestHelper.getRequest()),
				_objectDefinition2.getObjectDefinitionId(),
				ObjectActionKeys.ADD_OBJECT_ENTRY) &&
			!(StringUtil.equals(
				_objectDefinition1.getScope(),
				ObjectDefinitionConstants.SCOPE_COMPANY) &&
			  StringUtil.equals(
				  _objectDefinition2.getScope(),
				  ObjectDefinitionConstants.SCOPE_SITE)) &&
			StringUtil.equals(
				objectRelationship.getType(),
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY)) {

			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			creationMenu.addDropdownItem(
				dropdownItem -> {
					dropdownItem.setHref(
						PortletURLBuilder.create(
							PortalUtil.getControlPanelPortletURL(
								_customTabRequestHelper.getRequest(),
								serviceContext.getScopeGroup(),
								_objectDefinition2.getPortletId(), 0, 0,
								PortletRequest.RENDER_PHASE)
						).setMVCRenderCommandName(
							"/object_entries/edit_object_entry"
						).setBackURL(
							_customTabRequestHelper.getCurrentURL()
						).setParameter(
							ObjectFieldSettingConstants.
								NAME_OBJECT_RELATIONSHIP_ERC_OBJECT_FIELD_NAME,
							ObjectFieldSettingUtil.getValue(
								ObjectFieldSettingConstants.
									NAME_OBJECT_RELATIONSHIP_ERC_OBJECT_FIELD_NAME,
								ObjectFieldLocalServiceUtil.getObjectField(
									objectRelationship.getObjectFieldId2()))
						).setParameter(
							"objectDefinitionId",
							_objectDefinition2.getObjectDefinitionId()
						).setParameter(
							"parentObjectEntryERC",
							() -> {
								ObjectEntry objectEntry = getObjectEntry();

								return objectEntry.getExternalReferenceCode();
							}
						).setWindowState(
							WindowState.MAXIMIZED
						).buildString());
					dropdownItem.setLabel(
						LanguageUtil.get(
							_customTabRequestHelper.getRequest(),
							"create-new"));
				});
		}

		LiferayPortletResponse liferayPortletResponse =
			_customTabRequestHelper.getLiferayPortletResponse();

		creationMenu.addDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					liferayPortletResponse.getNamespace() +
						"selectRelatedModel");
				dropdownItem.setLabel(
					LanguageUtil.get(
						_customTabRequestHelper.getRequest(),
						"select-existing-one"));
				dropdownItem.setTarget("event");
			});

		return creationMenu;
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems()
		throws Exception {

		return Arrays.asList(
			new FDSActionDropdownItem(
				_getDeleteURL(_customTabRequestHelper.getRequest()), "trash",
				"delete",
				LanguageUtil.get(
					_customTabRequestHelper.getRequest(), "delete"),
				"post", "replace", "async"));
	}

	public ObjectEntry getObjectEntry() {
		String externalReferenceCode = ParamUtil.getString(
			_customTabRequestHelper.getRequest(), "externalReferenceCode");

		if (Validator.isNull(externalReferenceCode)) {
			HttpServletRequest httpServletRequest =
				_customTabRequestHelper.getRequest();

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
				_customTabRequestHelper.getCompanyId(), objectDefinition,
				String.valueOf(_getGroupId()));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}
		}

		return objectEntry;
	}

	public long getObjectEntryId() {
		ObjectEntry objectEntry = getObjectEntry();

		return objectEntry.getId();
	}

	public ObjectRelationship getObjectRelationship() throws PortalException {
		String relationshipName = ParamUtil.getString(
			_customTabRequestHelper.getRequest(),
			"screenNavigationCategoryKey");

		ObjectDefinition objectDefinition = _getObjectDefinition();

		return _objectRelationshipLocalService.getObjectRelationship(
			objectDefinition.getObjectDefinitionId(), relationshipName);
	}

	public String getRelatedObjectEntryItemSelectorURL()
		throws PortalException {

		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			RequestBackedPortletURLFactoryUtil.create(
				_customTabRequestHelper.getRequest());

		LiferayPortletResponse liferayPortletResponse =
			_customTabRequestHelper.getLiferayPortletResponse();

		InfoItemItemSelectorCriterion infoItemItemSelectorCriterion =
			new InfoItemItemSelectorCriterion();

		infoItemItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			Collections.singletonList(new InfoItemItemSelectorReturnType()));

		infoItemItemSelectorCriterion.setItemType(
			_objectDefinition1.getClassName());

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

	private String _getDeleteURL(HttpServletRequest httpServletRequest) {
		ObjectDefinition objectDefinition = _getObjectDefinition();

		return PortletURLBuilder.create(
			_portal.getControlPanelPortletURL(
				httpServletRequest, objectDefinition.getPortletId(),
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
			"objectEntryId", getObjectEntryId()
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

	private DTOConverterContext _getDTOConverterContext() {
		return new DefaultDTOConverterContext(
			false, null, null, _customTabRequestHelper.getRequest(), null,
			_themeDisplay.getLocale(), null, _themeDisplay.getUser());
	}

	private long _getGroupId() {
		ObjectScopeProvider objectScopeProvider =
			_objectScopeProviderRegistry.getObjectScopeProvider(
				_objectDefinition1.getScope());

		try {
			return objectScopeProvider.getGroupId(
				_customTabRequestHelper.getRequest());
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			return 0L;
		}
	}

	private ObjectDefinition _getObjectDefinition() {
		HttpServletRequest httpServletRequest =
			_customTabRequestHelper.getRequest();

		return (ObjectDefinition)httpServletRequest.getAttribute(
			"OBJECT_DEFINITION");
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CustomTabDisplayContext.class);

	private final CustomTabRequestHelper _customTabRequestHelper;
	private final ItemSelector _itemSelector;
	private final ObjectDefinition _objectDefinition1;
	private final ObjectDefinition _objectDefinition2;
	private final ObjectEntryManagerRegistry _objectEntryManagerRegistry;
	private final ObjectRelationshipLocalService
		_objectRelationshipLocalService;
	private final ObjectScopeProviderRegistry _objectScopeProviderRegistry;
	private final Portal _portal;
	private final ThemeDisplay _themeDisplay;

}