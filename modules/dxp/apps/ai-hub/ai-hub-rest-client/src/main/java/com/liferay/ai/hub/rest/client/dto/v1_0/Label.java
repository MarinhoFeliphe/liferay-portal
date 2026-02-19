/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.rest.client.dto.v1_0;

import com.liferay.ai.hub.rest.client.function.UnsafeSupplier;
import com.liferay.ai.hub.rest.client.serdes.v1_0.LabelSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Feliphe Marinho
 * @generated
 */
@Generated("")
public class Label implements Cloneable, Serializable {

	public static Label toDTO(String json) {
		return LabelSerDes.toDTO(json);
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public void setDisplayType(
		UnsafeSupplier<String, Exception> displayTypeUnsafeSupplier) {

		try {
			displayType = displayTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String displayType;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setValue(
		UnsafeSupplier<String, Exception> valueUnsafeSupplier) {

		try {
			value = valueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String value;

	@Override
	public Label clone() throws CloneNotSupportedException {
		return (Label)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Label)) {
			return false;
		}

		Label label = (Label)object;

		return Objects.equals(toString(), label.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return LabelSerDes.toJSON(this);
	}

}