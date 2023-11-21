/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.workflow;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Locale;
import java.util.Map;

/**
 * @author Paulo Albuquerque
 */
public class WorkflowLabelUtil {

	public static String getLabel(
		Map<Locale, String> labelMap, Locale locale, String name) {

		String label = labelMap.get(locale);

		if (label != null) {
			return HtmlUtil.escape(label);
		}

		Language language = LanguageUtil.getLanguage();

		if (name == null) {
			return HtmlUtil.escape(language.get(locale, "proceed"));
		}

		Locale defaultLocale = LocaleUtil.getSiteDefault();

		label = language.get(locale, name);

		if (!StringUtil.equalsIgnoreCase(
				label, language.get(defaultLocale, name))) {

			return HtmlUtil.escape(label);
		}

		label = labelMap.get(defaultLocale);

		if (label != null) {
			return HtmlUtil.escape(label);
		}

		return name;
	}

}