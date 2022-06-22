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

package com.liferay.object.admin.rest.client.dto.v1_0;

import com.liferay.object.admin.rest.client.function.UnsafeSupplier;
import com.liferay.object.admin.rest.client.serdes.v1_0.NextObjectStateSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class NextObjectState implements Cloneable, Serializable {

	public static NextObjectState toDTO(String json) {
		return NextObjectStateSerDes.toDTO(json);
	}

	public Long getListTypeEntryId() {
		return listTypeEntryId;
	}

	public void setListTypeEntryId(Long listTypeEntryId) {
		this.listTypeEntryId = listTypeEntryId;
	}

	public void setListTypeEntryId(
		UnsafeSupplier<Long, Exception> listTypeEntryIdUnsafeSupplier) {

		try {
			listTypeEntryId = listTypeEntryIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long listTypeEntryId;

	@Override
	public NextObjectState clone() throws CloneNotSupportedException {
		return (NextObjectState)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof NextObjectState)) {
			return false;
		}

		NextObjectState nextObjectState = (NextObjectState)object;

		return Objects.equals(toString(), nextObjectState.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return NextObjectStateSerDes.toJSON(this);
	}

}