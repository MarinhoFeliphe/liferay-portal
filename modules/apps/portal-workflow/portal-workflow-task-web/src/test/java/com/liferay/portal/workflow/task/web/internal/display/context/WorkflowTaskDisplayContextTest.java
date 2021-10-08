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

package com.liferay.portal.workflow.task.web.internal.display.context;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.LayoutRevision;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactory;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.util.FastDateFormatFactoryImpl;
import com.liferay.portal.workflow.LayoutRevisionWorkflowHandler;
import com.liferay.portal.workflow.UserWorkflowHandler;

import java.util.Arrays;
import java.util.TimeZone;

import javax.portlet.PortletRequest;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;
import org.mockito.Spy;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Feliphe Marinho
 */
public class WorkflowTaskDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		_setUpPortal();
		_setUpPortletPreferences();
		_setUpLiferayPortletRequest();
		_setUpLiferayPortletResponse();
		_setUpThemeDisplay();
		_setUpFastDateFormatFactory();
	}

	@Before
	public void setUp() {
		_workflowTaskDisplayContext = Mockito.spy(
			new WorkflowTaskDisplayContext(
				_liferayPortletRequest, _liferayPortletResponse));
	}

	@Test
	public void testGetAssetType() {
		Mockito.doReturn(
			LocaleUtil.BRAZIL
		).when(
			_workflowTaskDisplayContext
		).getTaskContentLocale();

		WorkflowHandler<?> userWorkflowHandler = Mockito.spy(
			new UserWorkflowHandler());

		Mockito.doReturn(
			User.class.getName()
		).when(
			userWorkflowHandler
		).getType(
			LocaleUtil.BRAZIL
		);

		WorkflowHandler<?> layoutRevisionWorkflowHandler = Mockito.spy(
			new LayoutRevisionWorkflowHandler());

		Mockito.doReturn(
			LayoutRevision.class.getName()
		).when(
			layoutRevisionWorkflowHandler
		).getType(
			LocaleUtil.BRAZIL
		);

		Mockito.doReturn(
			Arrays.asList(layoutRevisionWorkflowHandler, userWorkflowHandler)
		).when(
			_workflowTaskDisplayContext
		).getSearchableAssetsWorkflowHandlers();

		Assert.assertTrue(
			ArrayUtil.containsAll(
				_workflowTaskDisplayContext.getAssetType(StringPool.BLANK),
				new String[] {
					LayoutRevision.class.getName(), User.class.getName()
				}));

		Assert.assertTrue(
			ArrayUtil.containsAll(
				_workflowTaskDisplayContext.getAssetType(User.class.getName()),
				new String[] {User.class.getName()}));

		Assert.assertNull(
			_workflowTaskDisplayContext.getAssetType(
				RandomTestUtil.randomString()));
	}

	private static void _setUpFastDateFormatFactory() {
		ReflectionTestUtil.setFieldValue(
			FastDateFormatFactoryUtil.class, "_fastDateFormatFactory",
			new FastDateFormatFactoryImpl());
	}

	private static void _setUpLiferayPortletRequest() {
		_liferayPortletRequest = Mockito.mock(LiferayPortletRequest.class);

		Mockito.when(
			_portal.getHttpServletRequest(_liferayPortletRequest)
		).thenReturn(
			new MockHttpServletRequest()
		);
	}

	private static void _setUpLiferayPortletResponse() {
		_liferayPortletResponse = Mockito.mock(LiferayPortletResponse.class);

		Mockito.when(
			_portal.getHttpServletResponse(_liferayPortletResponse)
		).thenReturn(
			new MockHttpServletResponse()
		);
	}

	private static void _setUpPortal() {
		_portal = Mockito.mock(Portal.class);

		Mockito.when(
			_portal.getPortalURL((PortletRequest)Mockito.any())
		).thenReturn(
			RandomTestUtil.randomString()
		);

		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(_portal);
	}

	private static void _setUpPortletPreferences() {
		PortletPreferencesFactoryUtil portletPreferencesFactoryUtil =
			new PortletPreferencesFactoryUtil();

		PortletPreferencesFactory portletPreferencesFactory = Mockito.mock(
			PortletPreferencesFactory.class);

		portletPreferencesFactoryUtil.setPortletPreferencesFactory(
			portletPreferencesFactory);

		Mockito.when(
			portletPreferencesFactory.getPortalPreferences(
				Mockito.any(HttpServletRequest.class))
		).thenReturn(
			Mockito.mock(PortalPreferences.class)
		);
	}

	private static void _setUpThemeDisplay() {
		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.when(
			_liferayPortletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			themeDisplay
		);

		Mockito.when(
			themeDisplay.getLocale()
		).thenReturn(
			LocaleUtil.BRAZIL
		);

		Mockito.when(
			themeDisplay.getTimeZone()
		).thenReturn(
			TimeZone.getTimeZone("America/Recife")
		);
	}

	private static LiferayPortletRequest _liferayPortletRequest;
	private static LiferayPortletResponse _liferayPortletResponse;
	private static Portal _portal;

	@Spy
	private WorkflowTaskDisplayContext _workflowTaskDisplayContext;

}