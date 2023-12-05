/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {ApiHelpers} from '../helpers/ApiHelpers';

exports.test = test.extend({
	_api: async ({_cleanUpLeftOvers, page}, use) => {
		await use(new ApiHelpers(_cleanUpLeftOvers, page));
	},
	_cleanUpLeftOvers: async ({page}, use) => {
		const leftOvers = [];
		await use(leftOvers);
		while (leftOvers.length !== 0) {
			const leftOver = leftOvers.pop();
			await leftOver.cleanUpFunction(leftOver.payload);
		}
	},
});
