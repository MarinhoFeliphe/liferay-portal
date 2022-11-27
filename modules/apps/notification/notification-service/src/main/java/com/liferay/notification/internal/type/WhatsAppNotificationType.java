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
import com.liferay.notification.model.NotificationTemplate;
import com.liferay.notification.type.BaseNotificationType;
import com.liferay.notification.type.NotificationType;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Phone;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(immediate = true, service = NotificationType.class)
public class WhatsAppNotificationType extends BaseNotificationType {

	@Override
	public String getType() {
		return "whatsApp";
	}

	@Override
	public void sendNotification(NotificationContext notificationContext)
		throws PortalException {

		Twilio.init(System.getenv("ACCOUNT_SID"), System.getenv("AUTH_TOKEN"));

		NotificationTemplate notificationTemplate =
			notificationContext.getNotificationTemplate();

		Map<String, Object> termValues = notificationContext.getTermValues();

		User user = _userLocalService.getUser(
			GetterUtil.getLong(termValues.get("creator")));

		MessageCreator messageCreator = Message.creator(
			_getCreatorPhoneNumber(user),
			new PhoneNumber(System.getenv("TWILIO_PHONE_NUMBER")),
			formatContent(
				notificationTemplate.getBody(user.getLocale()),
				notificationContext));

		messageCreator.create();
	}

	private PhoneNumber _getCreatorPhoneNumber(User user)
		throws PortalException {

		List<Phone> phones = user.getPhones();

		Phone primaryPhone = phones.get(0);

		return new PhoneNumber("whatsapp:+" + primaryPhone.getNumber());
	}

	@Reference
	private UserLocalService _userLocalService;

}