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

package com.liferay.object.service.impl;

import com.liferay.object.model.ObjectState;
import com.liferay.object.service.ObjectStateTransitionLocalService;
import com.liferay.object.service.base.ObjectStateLocalServiceBaseImpl;
import com.liferay.portal.aop.AopService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = "model.class.name=com.liferay.object.model.ObjectState",
	service = AopService.class
)
public class ObjectStateLocalServiceImpl
	extends ObjectStateLocalServiceBaseImpl {

	@Override
	public ObjectState addObjectState(
		long listTypeEntryId, long objectStateFlowId, long userId,
		String userName) {

		ObjectState objectState = createObjectState(
			counterLocalService.increment());

		objectState.setUserId(userId);
		objectState.setUserName(userName);
		objectState.setListTypeEntryId(listTypeEntryId);
		objectState.setObjectStateFlowId(objectStateFlowId);

		return updateObjectState(objectState);
	}

	@Override
	public List<ObjectState> findByObjectStateFlowId(long objectStateFlowId) {
		return objectStatePersistence.findByObjectStateFlowId(
			objectStateFlowId);
	}

	@Override
	public List<ObjectState> getNextObjectStates(long sourceObjectStateId) {
		return Stream.of(
			_objectStateTransitionLocalService.findBySourceObjectStateId(
				sourceObjectStateId)
		).flatMap(
			List::stream
		).map(
			objectStateTransition -> objectStatePersistence.fetchByPrimaryKey(
				objectStateTransition.getTargetObjectStateId())
		).collect(
			Collectors.toList()
		);
	}

	@Reference
	private ObjectStateTransitionLocalService
		_objectStateTransitionLocalService;

}