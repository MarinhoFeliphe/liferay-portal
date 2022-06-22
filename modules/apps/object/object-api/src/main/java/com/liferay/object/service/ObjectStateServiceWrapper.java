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
 * Provides a wrapper for {@link ObjectStateService}.
 *
 * @author Marco Leo
 * @see ObjectStateService
 * @generated
 */
public class ObjectStateServiceWrapper
	implements ObjectStateService, ServiceWrapper<ObjectStateService> {

	public ObjectStateServiceWrapper() {
		this(null);
	}

	public ObjectStateServiceWrapper(ObjectStateService objectStateService) {
		_objectStateService = objectStateService;
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _objectStateService.getOSGiServiceIdentifier();
	}

	@Override
	public ObjectStateService getWrappedService() {
		return _objectStateService;
	}

	@Override
	public void setWrappedService(ObjectStateService objectStateService) {
		_objectStateService = objectStateService;
	}

	private ObjectStateService _objectStateService;

}