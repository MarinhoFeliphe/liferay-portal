/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.internal.upgrade.v4_1_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * @author Feliphe Marinho
 */
public class RoleProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		runSQL("DELETE FROM Role_ WHERE roleId = 36835 AND companyId = 20097");
		System.out.println("Executing process - RoleProcess");

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
			SQLTransformer.transform(
				"select roleId, name from Role_ where companyId = 20097"));
			 ResultSet resultSet = preparedStatement1.executeQuery()) {

			while (resultSet.next()) {
				System.out.println(
					"Upgrade process roleId: " + resultSet.getLong("roleId"));
				System.out.print(
					"Upgrade process name: " + resultSet.getLong("name"));
				System.out.println();
			}
		}
	}

}