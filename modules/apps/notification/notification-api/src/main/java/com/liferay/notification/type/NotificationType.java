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

package com.liferay.notification.type;

import com.liferay.notification.context.NotificationContext;
import com.liferay.notification.exception.NotificationQueueEntrySubjectException;
import com.liferay.notification.exception.NotificationTemplateAttachmentObjectFieldIdException;
import com.liferay.notification.exception.NotificationTemplateDescriptionException;
import com.liferay.notification.exception.NotificationTemplateEditorTypeException;
import com.liferay.notification.exception.NotificationTemplateNameException;
import com.liferay.notification.exception.NotificationTemplateObjectDefinitionIdException;
import com.liferay.notification.exception.NotificationTemplateSubjectException;
import com.liferay.notification.model.NotificationQueueEntry;
import com.liferay.notification.model.NotificationRecipientSetting;
import com.liferay.notification.model.NotificationTemplate;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.Validator;

import java.util.List;
import java.util.Objects;

/**
 * @author Feliphe Marinho
 */
public interface NotificationType {

	public NotificationQueueEntry createNotificationQueueEntry(
		User user, String body, NotificationContext notificationContext,
		String subject);

	public default String getFromName(
		NotificationQueueEntry notificationQueueEntry) {

		return "-";
	}

	public default String getRecipientSummary(
		NotificationQueueEntry notificationQueueEntry) {

		return "-";
	}

	public String getType();

	public String getTypeLanguageKey();

	public default void resendNotification(
			NotificationQueueEntry notificationQueueEntry)
		throws PortalException {
	}

	public default void resendNotifications(int status, String type)
		throws PortalException {
	}

	public void sendNotification(NotificationContext notificationContext)
		throws PortalException;

	public Object[] toRecipients(
		List<NotificationRecipientSetting> notificationRecipientSettings);

	public default void validateNotificationQueueEntry(
			NotificationContext notificationContext)
		throws PortalException {

		NotificationQueueEntry notificationQueueEntry =
			notificationContext.getNotificationQueueEntry();

		if (Validator.isNull(notificationQueueEntry.getSubject())) {
			throw new NotificationQueueEntrySubjectException("Subject is null");
		}
	}

	public default void validateNotificationTemplate(
			NotificationContext notificationContext)
		throws PortalException {

		NotificationTemplate notificationTemplate =
			notificationContext.getNotificationTemplate();

		if (notificationTemplate.getObjectDefinitionId() > 0) {
			ObjectDefinition objectDefinition =
				ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(
					notificationTemplate.getObjectDefinitionId());

			if (objectDefinition == null) {
				throw new NotificationTemplateObjectDefinitionIdException();
			}
		}

		String description = notificationTemplate.getDescription();

		if (description.length() > 255) {
			throw new NotificationTemplateDescriptionException(
				"The description cannot contain more than 255 characters");
		}

		if (Validator.isNull(notificationTemplate.getEditorType())) {
			throw new NotificationTemplateEditorTypeException(
				"Editor type is null");
		}

		if (Validator.isNull(notificationTemplate.getName())) {
			throw new NotificationTemplateNameException("Name is null");
		}

		if (Validator.isNull(notificationTemplate.getSubject())) {
			throw new NotificationTemplateSubjectException("Subject is null");
		}

		for (long attachmentObjectFieldId :
				notificationContext.getAttachmentObjectFieldIds()) {

			ObjectField objectField =
				ObjectFieldLocalServiceUtil.fetchObjectField(
					attachmentObjectFieldId);

			if ((objectField == null) ||
				!Objects.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_ATTACHMENT) ||
				!Objects.equals(
					objectField.getObjectDefinitionId(),
					notificationTemplate.getObjectDefinitionId())) {

				throw new NotificationTemplateAttachmentObjectFieldIdException();
			}
		}
	}

}