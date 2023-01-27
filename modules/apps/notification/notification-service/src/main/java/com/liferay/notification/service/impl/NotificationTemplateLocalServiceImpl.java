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

package com.liferay.notification.service.impl;

import com.liferay.notification.constants.NotificationTemplateConstants;
import com.liferay.notification.model.NotificationQueueEntry;
import com.liferay.notification.model.NotificationRecipient;
import com.liferay.notification.model.NotificationRecipientSetting;
import com.liferay.notification.model.NotificationTemplate;
import com.liferay.notification.model.NotificationTemplateAttachment;
import com.liferay.notification.service.NotificationRecipientLocalService;
import com.liferay.notification.service.NotificationRecipientSettingLocalService;
import com.liferay.notification.service.NotificationTemplateAttachmentLocalService;
import com.liferay.notification.service.base.NotificationTemplateLocalServiceBaseImpl;
import com.liferay.notification.service.persistence.NotificationQueueEntryPersistence;
import com.liferay.notification.service.persistence.NotificationTemplateAttachmentPersistence;
import com.liferay.notification.type.NotificationType;
import com.liferay.notification.type.NotificationTypeServiceTracker;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gabriel Albuquerque
 * @author Gustavo Lima
 */
@Component(
	property = "model.class.name=com.liferay.notification.model.NotificationTemplate",
	service = AopService.class
)
public class NotificationTemplateLocalServiceImpl
	extends NotificationTemplateLocalServiceBaseImpl {

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public NotificationTemplate addNotificationTemplate(
			List<Long> attachmentObjectFieldIds,
			NotificationRecipient notificationRecipient,
			List<NotificationRecipientSetting> notificationRecipientSettings,
			NotificationTemplate notificationTemplate)
		throws PortalException {

		_validate(
			attachmentObjectFieldIds, notificationRecipient,
			notificationRecipientSettings, notificationTemplate);

		notificationTemplate.setNotificationTemplateId(
			counterLocalService.increment());

		notificationTemplate = notificationTemplatePersistence.update(
			notificationTemplate);

		_resourceLocalService.addResources(
			notificationTemplate.getCompanyId(), 0,
			notificationTemplate.getUserId(),
			NotificationTemplate.class.getName(),
			notificationTemplate.getNotificationTemplateId(), false, true,
			true);

		notificationRecipient.setNotificationRecipientId(
			counterLocalService.increment());
		notificationRecipient.setClassNameId(
			_portal.getClassNameId(NotificationTemplate.class));
		notificationRecipient.setClassPK(
			notificationTemplate.getNotificationTemplateId());

		_notificationRecipientLocalService.updateNotificationRecipient(
			notificationRecipient);

		for (NotificationRecipientSetting notificationRecipientSetting :
				notificationRecipientSettings) {

			notificationRecipientSetting.setNotificationRecipientSettingId(
				counterLocalService.increment());
			notificationRecipientSetting.setNotificationRecipientId(
				notificationRecipient.getNotificationRecipientId());

			_notificationRecipientSettingLocalService.
				updateNotificationRecipientSetting(
					notificationRecipientSetting);
		}

		for (long attachmentObjectFieldId : attachmentObjectFieldIds) {
			_notificationTemplateAttachmentLocalService.
				addNotificationTemplateAttachment(
					notificationTemplate.getCompanyId(),
					notificationTemplate.getNotificationTemplateId(),
					attachmentObjectFieldId);
		}

		return notificationTemplate;
	}

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public NotificationTemplate addNotificationTemplate(
			String externalReferenceCode, long userId, String type)
		throws PortalException {

		NotificationTemplate notificationTemplate =
			notificationTemplatePersistence.create(
				counterLocalService.increment());

		notificationTemplate.setExternalReferenceCode(externalReferenceCode);

		User user = _userLocalService.getUser(userId);

		notificationTemplate.setUserId(userId);
		notificationTemplate.setUserName(user.getFullName());
		notificationTemplate.setEditorType(
			NotificationTemplateConstants.EDITOR_TYPE_RICH_TEXT);
		notificationTemplate.setName(externalReferenceCode);
		notificationTemplate.setType(type);

		notificationTemplate = notificationTemplatePersistence.update(
			notificationTemplate);

		_resourceLocalService.addResources(
			notificationTemplate.getCompanyId(), 0, userId,
			NotificationTemplate.class.getName(),
			notificationTemplate.getNotificationTemplateId(), false, true,
			true);

		NotificationRecipient notificationRecipient =
			_notificationRecipientLocalService.createNotificationRecipient(
				counterLocalService.increment());

		notificationRecipient.setUserId(userId);
		notificationRecipient.setUserName(user.getFullName());
		notificationRecipient.setClassNameId(
			_portal.getClassNameId(NotificationTemplate.class));
		notificationRecipient.setClassPK(
			notificationTemplate.getNotificationTemplateId());

		_notificationRecipientLocalService.updateNotificationRecipient(
			notificationRecipient);

		_addNotificationRecipientSetting(
			null, "from", notificationRecipient.getNotificationRecipientId(),
			user, externalReferenceCode);
		_addNotificationRecipientSetting(
			LocaleUtil.getDefault(), "fromName",
			notificationRecipient.getNotificationRecipientId(), user,
			externalReferenceCode);
		_addNotificationRecipientSetting(
			LocaleUtil.getDefault(), "to",
			notificationRecipient.getNotificationRecipientId(), user,
			externalReferenceCode);

		return notificationTemplate;
	}

	@Override
	public NotificationTemplate deleteNotificationTemplate(
			long notificationTemplateId)
		throws PortalException {

		NotificationTemplate notificationTemplate =
			notificationTemplatePersistence.findByPrimaryKey(
				notificationTemplateId);

		return notificationTemplateLocalService.deleteNotificationTemplate(
			notificationTemplate);
	}

	@Indexable(type = IndexableType.DELETE)
	@Override
	@SystemEvent(type = SystemEventConstants.TYPE_DELETE)
	public NotificationTemplate deleteNotificationTemplate(
			NotificationTemplate notificationTemplate)
		throws PortalException {

		notificationTemplate = notificationTemplatePersistence.remove(
			notificationTemplate);

		_resourceLocalService.deleteResource(
			notificationTemplate, ResourceConstants.SCOPE_INDIVIDUAL);

		NotificationRecipient notificationRecipient =
			notificationTemplate.getNotificationRecipient();

		_notificationRecipientLocalService.deleteNotificationRecipient(
			notificationRecipient);

		for (NotificationRecipientSetting notificationRecipientSetting :
				notificationRecipient.getNotificationRecipientSettings()) {

			_notificationRecipientSettingLocalService.
				deleteNotificationRecipientSetting(
					notificationRecipientSetting);
		}

		List<NotificationQueueEntry> notificationQueueEntries =
			_notificationQueueEntryPersistence.findByNotificationTemplateId(
				notificationTemplate.getNotificationTemplateId());

		for (NotificationQueueEntry notificationQueueEntry :
				notificationQueueEntries) {

			notificationQueueEntry.setNotificationTemplateId(0);

			_notificationQueueEntryPersistence.update(notificationQueueEntry);
		}

		_notificationTemplateAttachmentPersistence.
			removeByNotificationTemplateId(
				notificationTemplate.getNotificationTemplateId());

		return notificationTemplate;
	}

	@Override
	public NotificationTemplate getNotificationTemplate(
			long notificationTemplateId)
		throws PortalException {

		return notificationTemplatePersistence.findByPrimaryKey(
			notificationTemplateId);
	}

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public NotificationTemplate updateNotificationTemplate(
			List<Long> attachmentObjectFieldIds,
			NotificationRecipient notificationRecipient,
			List<NotificationRecipientSetting> notificationRecipientSettings,
			NotificationTemplate notificationTemplate)
		throws PortalException {

		_validate(
			attachmentObjectFieldIds, notificationRecipient,
			notificationRecipientSettings, notificationTemplate);

		notificationRecipient =
			_notificationRecipientLocalService.updateNotificationRecipient(
				notificationRecipient);

		for (NotificationRecipientSetting notificationRecipientSetting :
				notificationRecipient.getNotificationRecipientSettings()) {

			_notificationRecipientSettingLocalService.
				deleteNotificationRecipientSetting(
					notificationRecipientSetting.
						getNotificationRecipientSettingId());
		}

		for (NotificationRecipientSetting notificationRecipientSetting :
				notificationRecipientSettings) {

			notificationRecipientSetting.setNotificationRecipientSettingId(
				counterLocalService.increment());

			_notificationRecipientSettingLocalService.
				updateNotificationRecipientSetting(
					notificationRecipientSetting);
		}

		notificationTemplate = notificationTemplatePersistence.update(
			notificationTemplate);

		List<Long> oldAttachmentObjectFieldIds = new ArrayList<>();

		for (NotificationTemplateAttachment notificationTemplateAttachment :
				_notificationTemplateAttachmentPersistence.
					findByNotificationTemplateId(
						notificationTemplate.getNotificationTemplateId())) {

			if (ListUtil.exists(
					attachmentObjectFieldIds,
					attachmentObjectFieldId -> Objects.equals(
						attachmentObjectFieldId,
						notificationTemplateAttachment.getObjectFieldId()))) {

				oldAttachmentObjectFieldIds.add(
					notificationTemplateAttachment.getObjectFieldId());

				continue;
			}

			_notificationTemplateAttachmentPersistence.remove(
				notificationTemplateAttachment);
		}

		for (long attachmentObjectFieldId :
				ListUtil.remove(
					attachmentObjectFieldIds, oldAttachmentObjectFieldIds)) {

			_notificationTemplateAttachmentLocalService.
				addNotificationTemplateAttachment(
					notificationTemplate.getCompanyId(),
					notificationTemplate.getNotificationTemplateId(),
					attachmentObjectFieldId);
		}

		return notificationTemplate;
	}

	private void _addNotificationRecipientSetting(
		Locale locale, String name, long notificationRecipientId, User user,
		String value) {

		NotificationRecipientSetting notificationRecipientSetting =
			_notificationRecipientSettingLocalService.
				createNotificationRecipientSetting(
					counterLocalService.increment());

		notificationRecipientSetting.setUserId(user.getUserId());
		notificationRecipientSetting.setUserName(user.getFullName());
		notificationRecipientSetting.setNotificationRecipientId(
			notificationRecipientId);
		notificationRecipientSetting.setName(name);

		if (locale != null) {
			notificationRecipientSetting.setValue(
				value, LocaleUtil.getDefault());
		}
		else {
			notificationRecipientSetting.setValue(value);
		}

		_notificationRecipientSettingLocalService.
			updateNotificationRecipientSetting(notificationRecipientSetting);
	}

	private void _validate(
			List<Long> attachmentObjectFieldIds,
			NotificationRecipient notificationRecipient,
			List<NotificationRecipientSetting> notificationRecipientSettings,
			NotificationTemplate notificationTemplate)
		throws PortalException {

		NotificationType notificationType =
			_notificationTypeServiceTracker.getNotificationType(
				notificationTemplate.getType());

		notificationType.validateNotificationTemplate(
			attachmentObjectFieldIds, notificationRecipient,
			notificationRecipientSettings, notificationTemplate);
	}

	@Reference
	private NotificationQueueEntryPersistence
		_notificationQueueEntryPersistence;

	@Reference
	private NotificationRecipientLocalService
		_notificationRecipientLocalService;

	@Reference
	private NotificationRecipientSettingLocalService
		_notificationRecipientSettingLocalService;

	@Reference
	private NotificationTemplateAttachmentLocalService
		_notificationTemplateAttachmentLocalService;

	@Reference
	private NotificationTemplateAttachmentPersistence
		_notificationTemplateAttachmentPersistence;

	@Reference
	private NotificationTypeServiceTracker _notificationTypeServiceTracker;

	@Reference
	private Portal _portal;

	@Reference
	private ResourceLocalService _resourceLocalService;

	@Reference
	private UserLocalService _userLocalService;

}