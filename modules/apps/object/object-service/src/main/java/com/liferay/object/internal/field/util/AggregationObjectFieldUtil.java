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

package com.liferay.object.internal.field.util;

import com.liferay.object.field.setting.util.ObjectFieldSettingUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.relationship.util.ObjectRelationshipUtil;
import com.liferay.object.service.persistence.ObjectDefinitionPersistence;
import com.liferay.object.service.persistence.ObjectFieldPersistence;
import com.liferay.object.service.persistence.ObjectRelationshipPersistence;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Feliphe Marinho
 */
public class AggregationObjectFieldUtil {

	public static ObjectField getRelatedObjectField(
			ObjectDefinition objectDefinition,
			ObjectDefinitionPersistence objectDefinitionPersistence,
			ObjectFieldPersistence objectFieldPersistence,
			List<ObjectFieldSetting> objectFieldSettings,
			ObjectRelationshipPersistence objectRelationshipPersistence)
		throws PortalException {

		Map<String, Object> objectFieldSettingsValues =
			ObjectFieldSettingUtil.toMap(objectFieldSettings);

		ObjectRelationship objectRelationship =
			ObjectRelationshipUtil.getObjectRelationship(
				objectRelationshipPersistence.findByODI1_N(
					objectDefinition.getObjectDefinitionId(),
					GetterUtil.getString(
						objectFieldSettingsValues.get(
							"objectRelationshipName"))));

		ObjectDefinition relatedObjectDefinition =
			objectDefinitionPersistence.findByPrimaryKey(
				objectRelationship.getObjectDefinitionId2());

		return objectFieldPersistence.fetchByODI_N(
			relatedObjectDefinition.getObjectDefinitionId(),
			String.valueOf(objectFieldSettingsValues.get("objectFieldName")));
	}

}