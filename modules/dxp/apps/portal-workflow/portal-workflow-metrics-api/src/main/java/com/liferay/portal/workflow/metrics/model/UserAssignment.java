/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.workflow.metrics.model;

/**
 * @author Feliphe Marinho
 */
public class UserAssignment implements Assignment {

	public UserAssignment(long id, String name) {
		_id = id;
		_name = name;
	}

	@Override
	public long getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public void setId(long id) {
		_id = id;
	}

	public void setName(String name) {
		_name = name;
	}

	private long _id;
	private String _name;

}