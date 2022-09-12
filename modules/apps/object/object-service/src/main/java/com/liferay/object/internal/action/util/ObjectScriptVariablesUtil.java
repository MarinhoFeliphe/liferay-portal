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

package com.liferay.object.internal.action.util;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.object.system.SystemObjectDefinitionMetadata;
import com.liferay.object.system.SystemObjectDefinitionMetadataTracker;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Selton Guedes
 */
public class ObjectScriptVariablesUtil {

	public static Map<String, Object> toVariables(
		DTOConverterRegistry dtoConverterRegistry,
		ObjectDefinition objectDefinition, JSONObject payloadJSONObject,
		SystemObjectDefinitionMetadataTracker
			systemObjectDefinitionMetadataTracker,
		long userId) {

		Map<String, Object> allVariables = null;

		Map<String, Object> selectedVariables = new HashMap<>();

		if (objectDefinition.isSystem()) {
			String contentType = _getContentType(
				dtoConverterRegistry, objectDefinition,
				systemObjectDefinitionMetadataTracker);

			allVariables = HashMapBuilder.<String, Object>putAll(
				(Map<String, Object>)payloadJSONObject.get(
					"model" + objectDefinition.getName())
			).putAll(
				(Map<String, Object>)payloadJSONObject.get(
					"modelDTO" + contentType)
			).build();

			if (allVariables == null) {
				return new HashMap<>();
			}
		}
		else {
			allVariables = HashMapBuilder.<String, Object>putAll(
				(Map<String, Object>)payloadJSONObject.get("objectEntry")
			).build();

			allVariables.putAll(
				(Map<String, Object>)allVariables.get("values"));

			allVariables.remove("values");

			Object objectEntryId = allVariables.get("objectEntryId");

			if (objectEntryId != null) {
				selectedVariables.put("id", objectEntryId);
			}
		}

		List<ObjectField> objectFields =
			ObjectFieldLocalServiceUtil.getObjectFields(
				objectDefinition.getObjectDefinitionId());

		for (ObjectField objectField : objectFields) {
			if (!selectedVariables.containsKey(objectField.getName())) {
				Object value = allVariables.get(objectField.getName());

				selectedVariables.put(objectField.getName(), value);
			}
		}

		selectedVariables.put("creator", userId);

		return selectedVariables;
	}

	private static String _getContentType(
		DTOConverterRegistry dtoConverterRegistry,
		ObjectDefinition objectDefinition,
		SystemObjectDefinitionMetadataTracker
			systemObjectDefinitionMetadataTracker) {

		DTOConverter<?, ?> dtoConverter = dtoConverterRegistry.getDTOConverter(
			objectDefinition.getClassName());

		if (dtoConverter == null) {
			SystemObjectDefinitionMetadata systemObjectDefinitionMetadata =
				systemObjectDefinitionMetadataTracker.
					getSystemObjectDefinitionMetadata(
						objectDefinition.getName());

			return systemObjectDefinitionMetadata.getModelClassName();
		}

		return dtoConverter.getContentType();
	}

}