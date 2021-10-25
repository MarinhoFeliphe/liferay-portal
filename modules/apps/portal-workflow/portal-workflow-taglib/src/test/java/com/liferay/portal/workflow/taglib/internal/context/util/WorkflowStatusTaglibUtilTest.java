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

package com.liferay.portal.workflow.taglib.internal.context.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.bean.BeanProperties;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalService;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.workflow.taglib.WorkflowStatusTaglibWebKeys;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.mockito.Mockito;

/**
 * @author Feliphe Marinho
 */
public class WorkflowStatusTaglibUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetStatus() {
		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		BeanProperties beanProperties = Mockito.mock(BeanProperties.class);

		Mockito.when(
			beanProperties.getInteger(
				Mockito.anyObject(), Mockito.eq("status"))
		).thenReturn(
			2
		);

		ReflectionTestUtil.setFieldValue(
			BeanPropertiesUtil.class, "_beanProperties", beanProperties);

		WorkflowStatusTaglibUtilTest.mockAttribute(
			WorkflowStatusTaglibWebKeys.BEAN, httpServletRequest,
			Mockito.mock(Object.class));

		WorkflowStatusTaglibUtilTest.mockAttribute(
			WorkflowStatusTaglibWebKeys.STATUS, httpServletRequest,
			1);

		Assert.assertEquals(
			Integer.valueOf(2),
			WorkflowStatusTaglibUtil.getStatus(httpServletRequest));

		WorkflowStatusTaglibUtilTest.mockAttribute(
			WorkflowStatusTaglibWebKeys.BEAN, httpServletRequest,
			null);

		Assert.assertEquals(
			Integer.valueOf(1),
			WorkflowStatusTaglibUtil.getStatus(httpServletRequest));
	}

	@Test
	public void testGetStatusMessage() {
		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		WorkflowStatusTaglibUtilTest.mockAttribute(
			WorkflowStatusTaglibWebKeys.STATUS_MESSAGE,
			httpServletRequest, "Status Message");

		WorkflowStatusTaglibUtilTest.mockAttribute(
			WorkflowStatusTaglibWebKeys.STATUS, httpServletRequest,
			2);

		Assert.assertEquals(
			"Status Message",
			WorkflowStatusTaglibUtil.getStatusMessage(httpServletRequest));

		WorkflowStatusTaglibUtilTest.mockAttribute(
			WorkflowStatusTaglibWebKeys.STATUS_MESSAGE,
			httpServletRequest, StringPool.BLANK);

		Assert.assertEquals(
			WorkflowConstants.LABEL_DRAFT,
			WorkflowStatusTaglibUtil.getStatusMessage(httpServletRequest));
	}

	protected static void mockAttribute(
		String attribute,
		HttpServletRequest httpServletRequest, Object value) {

		Mockito.when(
			httpServletRequest.getAttribute(
				Mockito.eq(WorkflowStatusTaglibUtil.ATTRIBUTE_NAMESPACE + attribute))
		).thenReturn(
			value
		);
	}

}