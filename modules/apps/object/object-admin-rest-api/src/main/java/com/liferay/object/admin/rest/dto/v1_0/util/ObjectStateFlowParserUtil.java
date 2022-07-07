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

package com.liferay.object.admin.rest.dto.v1_0.util;

import com.liferay.object.admin.rest.dto.v1_0.NextObjectState;
import com.liferay.object.model.ObjectState;
import com.liferay.object.model.ObjectStateFlow;
import com.liferay.object.service.ObjectStateLocalServiceUtil;
import com.liferay.portal.vulcan.util.TransformUtil;

/**
 * @author Feliphe Marinho
 */
public class ObjectStateFlowParserUtil {

	public static String parse(ObjectStateFlow objectStateFlow) {
		return com.liferay.object.util.JSONUtil.getJSONString(
			new com.liferay.object.admin.rest.dto.v1_0.ObjectStateFlow() {
				{
					id = objectStateFlow.getObjectStateFlowId();
					objectStates = TransformUtil.transformToArray(
						ObjectStateLocalServiceUtil.getObjectStateFlowObjectStates(
							objectStateFlow.getObjectStateFlowId()),
						ObjectStateFlowParserUtil::_toObjectState,
						com.liferay.object.admin.rest.dto.v1_0.ObjectState.class);
				}
			});
	}

	private static NextObjectState _toNextObjectState(
		ObjectState nextObjectState) {
		return new NextObjectState() {
			{
				listTypeEntryId = nextObjectState.getListTypeEntryId();
			}
		};
	}

	private static com.liferay.object.admin.rest.dto.v1_0.ObjectState _toObjectState(
		ObjectState objectState) {

		return new com.liferay.object.admin.rest.dto.v1_0.ObjectState() {
			{
				id = objectState.getObjectStateId();
				listTypeEntryId = objectState.getListTypeEntryId();
				nextObjectStates = TransformUtil.transformToArray(
					ObjectStateLocalServiceUtil.getNextObjectStates(
						objectState.getObjectStateId()),
					ObjectStateFlowParserUtil::_toNextObjectState,
					NextObjectState.class);
			}
		};
	}
}