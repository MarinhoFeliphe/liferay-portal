/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.runtime.notification;

import com.liferay.portal.test.rule.LiferayUnitTestRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Victor Kammerer
 */
public class BaseNotificationSenderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetNotificationRecipientsMap() throws Exception {
		_baseNotificationSender.getNotificationRecipientsMap(
			null, null);
	}

	private final BaseNotificationSender _baseNotificationSender =
		Mockito.spy(BaseNotificationSender.class);;
}