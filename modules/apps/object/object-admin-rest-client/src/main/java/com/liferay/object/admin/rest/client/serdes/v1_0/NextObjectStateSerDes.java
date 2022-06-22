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

package com.liferay.object.admin.rest.client.serdes.v1_0;

import com.liferay.object.admin.rest.client.dto.v1_0.NextObjectState;
import com.liferay.object.admin.rest.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class NextObjectStateSerDes {

	public static NextObjectState toDTO(String json) {
		NextObjectStateJSONParser nextObjectStateJSONParser =
			new NextObjectStateJSONParser();

		return nextObjectStateJSONParser.parseToDTO(json);
	}

	public static NextObjectState[] toDTOs(String json) {
		NextObjectStateJSONParser nextObjectStateJSONParser =
			new NextObjectStateJSONParser();

		return nextObjectStateJSONParser.parseToDTOs(json);
	}

	public static String toJSON(NextObjectState nextObjectState) {
		if (nextObjectState == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (nextObjectState.getListTypeEntryId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"listTypeEntryId\": ");

			sb.append(nextObjectState.getListTypeEntryId());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		NextObjectStateJSONParser nextObjectStateJSONParser =
			new NextObjectStateJSONParser();

		return nextObjectStateJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(NextObjectState nextObjectState) {
		if (nextObjectState == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (nextObjectState.getListTypeEntryId() == null) {
			map.put("listTypeEntryId", null);
		}
		else {
			map.put(
				"listTypeEntryId",
				String.valueOf(nextObjectState.getListTypeEntryId()));
		}

		return map;
	}

	public static class NextObjectStateJSONParser
		extends BaseJSONParser<NextObjectState> {

		@Override
		protected NextObjectState createDTO() {
			return new NextObjectState();
		}

		@Override
		protected NextObjectState[] createDTOArray(int size) {
			return new NextObjectState[size];
		}

		@Override
		protected void setField(
			NextObjectState nextObjectState, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "listTypeEntryId")) {
				if (jsonParserFieldValue != null) {
					nextObjectState.setListTypeEntryId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			Class<?> valueClass = value.getClass();

			if (value instanceof Map) {
				sb.append(_toJSON((Map)value));
			}
			else if (valueClass.isArray()) {
				Object[] values = (Object[])value;

				sb.append("[");

				for (int i = 0; i < values.length; i++) {
					sb.append("\"");
					sb.append(_escape(values[i]));
					sb.append("\"");

					if ((i + 1) < values.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(entry.getValue()));
				sb.append("\"");
			}
			else {
				sb.append(String.valueOf(entry.getValue()));
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

}