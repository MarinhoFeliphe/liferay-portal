/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {ModelBuilderPage} from '../pages/objects/modelBuilder.page';
import {ObjectDefinitionsPage} from '../pages/objects/objectDefinitions.page';

exports.test = test.extend({
	_cleanUpLeftOvers: async ({page}, use) => {
		const leftOvers = [];
		await use(leftOvers);
		while (leftOvers.length !== 0) {
			const leftOver = leftOvers.pop();
			await leftOver.cleanUpFunction(leftOver.payload);
		}
	},
	_modelBuilderPage: async ({page}, use) => {
		await use(new ModelBuilderPage(page));
	},
	_objectDefinitionsPage: async ({_cleanUpLeftOvers, page}, use) => {
		await use(new ObjectDefinitionsPage(_cleanUpLeftOvers, page));
	},
});
