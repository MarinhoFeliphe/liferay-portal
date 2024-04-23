/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.script.management.groovy.script.uses.factory;

import com.liferay.portal.security.script.management.groovy.script.use.GroovyScriptUse;

import java.util.List;
import java.util.Locale;

/**
 * @author Feliphe Marinho
 */
public interface GroovyScriptUsesFactoryRegistry {

	public List<GroovyScriptUse> getGroovyScriptUses(Locale locale)
		throws Exception;

}