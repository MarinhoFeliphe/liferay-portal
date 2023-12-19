/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.internal.upgrade.v4_1_0;

import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Feliphe Marinho
 */
public class RoleProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		runSQL("DELETE FROM Role_ WHERE roleId = 36835 AND companyId = 20097");
	}

}