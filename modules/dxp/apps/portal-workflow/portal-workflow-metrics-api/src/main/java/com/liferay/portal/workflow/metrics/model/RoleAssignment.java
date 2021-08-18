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

import java.util.List;

/**
 * @author Feliphe Marinho
 */
public class RoleAssignment implements Assignment {

	public RoleAssignment(List<Long> groupIds, long id) {
		_groupIds = groupIds;
		_id = id;
	}

	public List<Long> getGroupIds() {
		return _groupIds;
	}

	@Override
	public long getId() {
		return _id;
	}

	public void setGroupId(List<Long> groupIds) {
		_groupIds = groupIds;
	}

	public void setId(long id) {
		_id = id;
	}

	private List<Long> _groupIds;
	private long _id;

}