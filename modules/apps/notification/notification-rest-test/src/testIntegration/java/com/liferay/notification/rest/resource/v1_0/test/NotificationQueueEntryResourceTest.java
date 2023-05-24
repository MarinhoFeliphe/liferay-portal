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

package com.liferay.notification.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.notification.constants.NotificationConstants;
import com.liferay.notification.rest.client.dto.v1_0.NotificationQueueEntry;
import com.liferay.notification.rest.client.problem.Problem;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Gabriel Albuquerque
 */
@RunWith(Arquillian.class)
public class NotificationQueueEntryResourceTest
	extends BaseNotificationQueueEntryResourceTestCase {

	@Test
	public void testPostNotificationQueueEntry() throws Exception {
		NotificationQueueEntry randomNotificationQueueEntry =
			randomNotificationQueueEntry();

		randomNotificationQueueEntry.setType(
			NotificationConstants.TYPE_USER_NOTIFICATION);

		try {
			notificationQueueEntryResource.postNotificationQueueEntry(
				randomNotificationQueueEntry);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}

		randomNotificationQueueEntry.setType(NotificationConstants.TYPE_EMAIL);

		NotificationQueueEntry postNotificationQueueEntry =
			testPostNotificationQueueEntry_addNotificationQueueEntry(
				randomNotificationQueueEntry);

		assertEquals(randomNotificationQueueEntry, postNotificationQueueEntry);
		assertValid(postNotificationQueueEntry);
	}

	@Override
	protected NotificationQueueEntry randomNotificationQueueEntry()
		throws Exception {

		NotificationQueueEntry notificationQueueEntry =
			super.randomNotificationQueueEntry();

//		JSONArray JSONArray = JSONFactoryUtil.createJSONArray();
//
//		JSONArray.put(JSONFactoryUtil.
//			createJSONObject(
//				HashMapBuilder.put("to", "to").build()));

		notificationQueueEntry.setRecipients(
			new Object[]{
				JSONFactoryUtil.
					createJSONObject(
					HashMapBuilder.put("to", "to").build())
		});
		notificationQueueEntry.setType(NotificationConstants.TYPE_EMAIL);

		return notificationQueueEntry;
	}

	@Override
	protected NotificationQueueEntry
			testDeleteNotificationQueueEntry_addNotificationQueueEntry()
		throws Exception {

		return _addNotificationQueueEntry(randomNotificationQueueEntry());
	}

	@Override
	protected NotificationQueueEntry
			testGetNotificationQueueEntriesPage_addNotificationQueueEntry(
				NotificationQueueEntry notificationQueueEntry)
		throws Exception {

		return _addNotificationQueueEntry(notificationQueueEntry);
	}

	@Override
	protected NotificationQueueEntry
			testGetNotificationQueueEntry_addNotificationQueueEntry()
		throws Exception {

		return _addNotificationQueueEntry(randomNotificationQueueEntry());
	}

	@Override
	protected NotificationQueueEntry
			testGraphQLNotificationQueueEntry_addNotificationQueueEntry()
		throws Exception {

		return _addNotificationQueueEntry(randomNotificationQueueEntry());
	}

	@Override
	protected NotificationQueueEntry
			testPostNotificationQueueEntry_addNotificationQueueEntry(
				NotificationQueueEntry notificationQueueEntry)
		throws Exception {

		return _addNotificationQueueEntry(notificationQueueEntry);
	}

	@Override
	protected NotificationQueueEntry
			testPutNotificationQueueEntryResend_addNotificationQueueEntry()
		throws Exception {

		return _addNotificationQueueEntry(randomNotificationQueueEntry());
	}

	private NotificationQueueEntry _addNotificationQueueEntry(
			NotificationQueueEntry notificationQueueEntry)
		throws Exception {

		return notificationQueueEntryResource.postNotificationQueueEntry(
			notificationQueueEntry);
	}

}