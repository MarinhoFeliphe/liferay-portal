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

package com.liferay.object.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link ObjectStateTransitionService}.
 *
 * @author Marco Leo
 * @see ObjectStateTransitionService
 * @generated
 */
public class ObjectStateTransitionServiceWrapper
	implements ObjectStateTransitionService,
			   ServiceWrapper<ObjectStateTransitionService> {

	public ObjectStateTransitionServiceWrapper() {
		this(null);
	}

	public ObjectStateTransitionServiceWrapper(
		ObjectStateTransitionService objectStateTransitionService) {

		_objectStateTransitionService = objectStateTransitionService;
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _objectStateTransitionService.getOSGiServiceIdentifier();
	}

	@Override
	public ObjectStateTransitionService getWrappedService() {
		return _objectStateTransitionService;
	}

	@Override
	public void setWrappedService(
		ObjectStateTransitionService objectStateTransitionService) {

		_objectStateTransitionService = objectStateTransitionService;
	}

	private ObjectStateTransitionService _objectStateTransitionService;

}