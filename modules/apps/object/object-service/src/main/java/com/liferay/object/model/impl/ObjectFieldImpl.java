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

package com.liferay.object.model.impl;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.List;
import java.util.Objects;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
public class ObjectFieldImpl extends ObjectFieldBaseImpl {

	@Override
	public ObjectDefinition getObjectDefinition() throws PortalException {
		return ObjectDefinitionLocalServiceUtil.getObjectDefinition(
			getObjectDefinitionId());
	}

	@Override
	public List<ObjectFieldSetting> getObjectFieldSettings() {
		return _objectFieldSettings;
	}

	@Override
	public boolean isAggregation() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_AGGREGATION)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isAttachment() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_ATTACHMENT)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isBoolean() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isDate() {
		if (Objects.equals(
				getBusinessType(), ObjectFieldConstants.BUSINESS_TYPE_DATE)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isDecimal() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_DECIMAL)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isFormula() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_FORMULA)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isInteger() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_INTEGER)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isLargeFile() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_LARGE_FILE)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isLongInteger() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_LONG_INTEGER)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isLongText() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isMultiselectPicklist() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_MULTISELECT_PICKLIST)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isPrecisionDecimal() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_PRECISION_DECIMAL)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isRelationship() {
		if (Objects.equals(
				getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_RELATIONSHIP)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean isText() {
		if (Objects.equals(
				getBusinessType(), ObjectFieldConstants.BUSINESS_TYPE_TEXT)) {

			return true;
		}

		return false;
	}

	@Override
	public void setObjectFieldSettings(
		List<ObjectFieldSetting> objectFieldSettings) {

		_objectFieldSettings = objectFieldSettings;
	}

	private List<ObjectFieldSetting> _objectFieldSettings;

}