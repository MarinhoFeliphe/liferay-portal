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

package com.liferay.portal.workflow.taglib.internal.display.context;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.bean.BeanProperties;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.workflow.taglib.internal.constants.WorkflowStatusWebKeys;
import com.liferay.portal.workflow.taglib.internal.context.util.WorkflowStatusDisplayContext;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Feliphe Marinho
 */
public class WorkflowStatusDisplayContextTest {

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
			beanProperties.getInteger(Mockito.anyObject(), Mockito.eq("status"))
		).thenReturn(
			2
		);

		ReflectionTestUtil.setFieldValue(
			BeanPropertiesUtil.class, "_beanProperties", beanProperties);

		_mockAttribute(
			WorkflowStatusWebKeys.BEAN, httpServletRequest,
			Mockito.mock(Object.class));

		_mockAttribute(WorkflowStatusWebKeys.STATUS, httpServletRequest, 1);

		Assert.assertEquals(
			Integer.valueOf(2),
			WorkflowStatusDisplayContext.getStatus(httpServletRequest));

		_mockAttribute(WorkflowStatusWebKeys.BEAN, httpServletRequest, null);

		Assert.assertEquals(
			Integer.valueOf(1),
			WorkflowStatusDisplayContext.getStatus(httpServletRequest));
	}

	@Test
	public void testGetStatusMessage() {
		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		_mockAttribute(
			WorkflowStatusWebKeys.STATUS_MESSAGE, httpServletRequest,
			"Status Message");

		_mockAttribute(WorkflowStatusWebKeys.STATUS, httpServletRequest, 2);

		Assert.assertEquals(
			"Status Message",
			WorkflowStatusDisplayContext.getStatusMessage(httpServletRequest));

		_mockAttribute(
			WorkflowStatusWebKeys.STATUS_MESSAGE, httpServletRequest,
			StringPool.BLANK);

		Assert.assertEquals(
			WorkflowConstants.LABEL_DRAFT,
			WorkflowStatusDisplayContext.getStatusMessage(httpServletRequest));
	}

	private void _mockAttribute(
		String attribute, HttpServletRequest httpServletRequest, Object value) {

		Mockito.when(
			httpServletRequest.getAttribute(
				Mockito.eq(
					WorkflowStatusDisplayContext.ATTRIBUTE_NAMESPACE +
						attribute))
		).thenReturn(
			value
		);
	}

}