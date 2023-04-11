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

package com.liferay.object.internal.upgrade.v5_1_2;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Feliphe Marinho
 */
public class ObjectFieldUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		String selectSQL = SQLTransformer.transform(
			StringBundler.concat(
				"select ObjectField.dbColumnName,ObjectField.dbTableName from ",
				"ObjectField where ObjectField.businessType = 'Relationship' ",
				"and ObjectField.relationshipType = 'oneToMany' union select ",
				"ObjectField.dbColumnName,ObjectField.dbTableName from ",
				"ObjectField where ObjectField.name in (select ",
				"ObjectFieldSetting.value from ObjectField inner join ",
				"ObjectFieldSetting on ObjectField.objectFieldId = ",
				"ObjectFieldSetting.objectFieldId where ",
				"ObjectField.businessType = 'Aggregation' and ",
				"ObjectFieldSetting.name = 'objectFieldName' and ",
				"ObjectFieldSetting.value != 'id')"));

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				selectSQL);
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update ...");
			ResultSet resultSet = preparedStatement1.executeQuery()) {

			while (resultSet.next()) {

				preparedStatement2.addBatch();
			}

			preparedStatement2.executeBatch();
		}
	}

}