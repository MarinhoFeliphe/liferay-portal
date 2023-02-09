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

import com.liferay.frontend.taglib.servlet.taglib.ScreenNavigationCategory;
import com.liferay.frontend.taglib.servlet.taglib.ScreenNavigationEntry;
import com.liferay.frontend.taglib.servlet.taglib.util.JSPRenderer;
import com.liferay.item.selector.ItemSelector;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectLayoutTab;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.kernel.exception.PortalException;

import java.io.IOException;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(
	property = {
		"screen.navigation.category.order:Integer=8000",
		"screen.navigation.entry.order:Integer=8000"
	},
	service = {ScreenNavigationCategory.class, ScreenNavigationEntry.class}
)
public class CustomTabScreenNavigationCategory
	implements ScreenNavigationCategory,
			   ScreenNavigationEntry<ObjectLayoutTab> {

	@Override
	public String getCategoryKey() {
		return "authorBook";
	}

	@Override
	public String getEntryKey() {
		return "authorBook";
	}

	@Override
	public String getLabel(Locale locale) {
		return "Authors (Custom Tab)";
	}

	@Override
	public String getScreenNavigationKey() {
		ObjectDefinition objectDefinition = _getObjectDefinition();

		return objectDefinition.getClassName();
	}

	@Override
	public void render(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		ObjectDefinition objectDefinition = _getObjectDefinition();

		httpServletRequest.setAttribute(
			"displayContext",
			new FooDisplayContext(
				httpServletRequest, _itemSelector, _objectEntryManagerRegistry,
				objectDefinition, _objectRelationshipLocalService,
				_objectScopeProviderRegistry));

		_jspRenderer.renderJSP(
			_servletContext, httpServletRequest, httpServletResponse,
			"/custom/relationship_tab.jsp");
	}

	private ObjectDefinition _getObjectDefinition() {
		try {
			return _objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"b12f882d-a83b-c450-0bb4-fdaf525f366d", 20096L);
		}
		catch (PortalException portalException) {
			return ReflectionUtil.throwException(portalException);
		}
	}

	@Reference
	private ItemSelector _itemSelector;

	@Reference
	private JSPRenderer _jspRenderer;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Reference
	private ObjectScopeProviderRegistry _objectScopeProviderRegistry;

	@Reference(target = "(osgi.web.symbolicname=com.liferay.notification.web)")
	private ServletContext _servletContext;

}