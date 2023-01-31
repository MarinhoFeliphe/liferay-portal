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

package com.liferay.object.internal.layout.tab.screen.navigation;

import com.liferay.frontend.taglib.servlet.taglib.ScreenNavigationCategory;
import com.liferay.object.model.ObjectLayoutTab;

import java.util.Locale;

/**
 * @author Feliphe Marinho
 */
public class ObjectLayoutTabScreenNavigationCategory
	implements ScreenNavigationCategory {

	public ObjectLayoutTabScreenNavigationCategory(
		ObjectLayoutTab objectLayoutTab) {

		_objectLayoutTab = objectLayoutTab;
	}

	@Override
	public String getCategoryKey() {
		return String.valueOf(_objectLayoutTab.getObjectLayoutTabId());
	}

	@Override
	public String getLabel(Locale locale) {
		return _objectLayoutTab.getName(locale);
	}

	@Override
	public String getScreenNavigationKey() {
		return String.valueOf(_objectLayoutTab.getObjectLayoutId());
	}

	private final ObjectLayoutTab _objectLayoutTab;

}