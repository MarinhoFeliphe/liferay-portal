/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.runtime.integration.internal;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.CodeCoverageAssertor;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.workflow.WorkflowLabelUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Paulo Albuquerque
 */
public class WorkflowLabelUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		_setUpLanguageUtil();
	}

	@Test
	public void testGetLabel() {

		// Label must be the labelMap value for locale

		Map<Locale, String> labelMap = HashMapBuilder.put(
			LocaleUtil.BRAZIL, "Tarefa de aprovar"
		).build();

		String label = WorkflowLabelUtil.getLabel(labelMap, LocaleUtil.BRAZIL, null);

		Assert.assertEquals("Tarefa de aprovar", label);

		// label must be 'Proceed'

		label = WorkflowLabelUtil.getLabel(new HashMap<>(), LocaleUtil.US, null);

		Assert.assertEquals("Proceed", label);

		// Label must be the language key value for locale

		label = WorkflowLabelUtil.getLabel(new HashMap<>(), LocaleUtil.BRAZIL, "approve");

		Assert.assertEquals("Aprovar", label);

		// Label must be the labelMap value for default locale

		labelMap = HashMapBuilder.put(
			LocaleUtil.US, "Approve task"
		).build();

		label = WorkflowLabelUtil.getLabel(labelMap, _locale, "approve");

		Assert.assertEquals("Approve task", label);

		// Label must be the name

		label = WorkflowLabelUtil.getLabel(new HashMap<>(), _locale, "approve");

		Assert.assertEquals("approve", label);
	}

	private static void _setUpLanguageUtil() {
		LanguageUtil languageUtil = new LanguageUtil();

		Language language = Mockito.mock(Language.class);

		Mockito.when(
			language.get(LocaleUtil.BRAZIL, "approve")
		).thenReturn(
			"Aprovar"
		);

		Mockito.when(
			language.get(LocaleUtil.US, "approve")
		).thenReturn(
			"Approve"
		);

		Mockito.when(
			language.get(LocaleUtil.US, "proceed")
		).thenReturn(
			"Proceed"
		);

		Mockito.when(
			language.get(_locale, "approve")
		).thenReturn(
			"Approve"
		);

		languageUtil.setLanguage(language);
	}

	private static final Locale _locale = new Locale("aa", "AA");
}
