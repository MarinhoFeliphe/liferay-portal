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

package com.liferay.object.admin.rest.internal.dto.v1_0.util;

import com.liferay.object.admin.rest.dto.v1_0.ObjectState;
import com.liferay.object.model.ObjectStateFlow;
import com.liferay.object.service.ObjectStateFlowLocalService;
import com.liferay.object.service.ObjectStateLocalService;
import com.liferay.object.service.ObjectStateTransitionLocalService;

/**
 * @author Feliphe Marinho
 */
public class ObjectStateFlowUtil {

	public static ObjectStateFlow toObjectStateFlow(
		com.liferay.object.admin.rest.dto.v1_0.ObjectStateFlow
			objectStateFlowDTO,
		ObjectStateLocalService objectStateLocalService,
		ObjectStateFlowLocalService objectStateFlowLocalService,
		ObjectStateTransitionLocalService objectStateTransitionLocalService) {

		ObjectStateFlow objectStateFlow =
			objectStateFlowLocalService.createObjectStateFlow(0L);

		for (ObjectState objectStateDTO :
				objectStateFlowDTO.getObjectStates()) {

			com.liferay.object.model.ObjectState objectState =
				objectStateLocalService.createObjectState(0L);

			objectState.setObjectStateFlowId(
				objectStateFlow.getObjectStateFlowId());
		}

		return null;
	}

}