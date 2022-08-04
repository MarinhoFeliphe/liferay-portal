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

package com.liferay.object.runtime.scripting.internal.validator;

import com.liferay.object.runtime.scripting.validator.GroovyScriptingValidator;
import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Feliphe Marinho
 */
public class GroovyScriptingValidatorImpl implements GroovyScriptingValidator {

	private final int _MAXIMUM_NUMBER_OF_LINES = 2987;

	@Override
	public void validate(String script) throws PortalException {
		//TODO validate syntax and maximum number of lines here
	}
}
