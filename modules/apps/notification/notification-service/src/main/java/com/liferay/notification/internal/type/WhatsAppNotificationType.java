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

package com.liferay.notification.internal.type;

import com.liferay.notification.context.NotificationContext;
import com.liferay.notification.model.NotificationRecipientSetting;
import com.liferay.notification.model.NotificationTemplate;
import com.liferay.notification.type.BaseNotificationType;
import com.liferay.notification.type.NotificationType;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(
	immediate = true, property = "notification.type.key=whatsApp",
	service = NotificationType.class
)
public class WhatsAppNotificationType extends BaseNotificationType {

	@Override
	public String getType() {
		return "whatsApp";
	}

	@Override
	public String getTypeLanguageKey() {
		return "WhatsApp";
	}

	@Override
	public void sendNotification(NotificationContext notificationContext)
		throws PortalException {

		Twilio.init(_ACCOUNT_SID, _AUTH_TOKEN);

		User user = _userLocalService.getUser(notificationContext.getUserId());

		NotificationTemplate notificationTemplate =
			notificationContext.getNotificationTemplate();

		Message.creator(
			new PhoneNumber("whatsapp:+"),
			new PhoneNumber("whatsapp:+14155238886"),
			notificationTemplate.getBody(user.getLocale())
		).create();
	}

	private static final String _ACCOUNT_SID = "";

	private static final String _AUTH_TOKEN = "";

	@Reference
	private UserLocalService _userLocalService;

}