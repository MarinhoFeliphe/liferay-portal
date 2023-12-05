/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const teardown = async ({request}) => {
	const headers = {
		Authorization: 'Basic ' + btoa('test@liferay.com:test'),
	};

	const objectAdminBaseURL = 'http://localhost:8080/o/object-admin/v1.0';

	const objectDefinitionsResponse = await request.get(
		`${objectAdminBaseURL}/object-definitions`,
		{headers}
	);

	const objectDefinitions = await objectDefinitionsResponse.json();

	const customObjectDefinitions = objectDefinitions.items.filter(
		(objectDefinition) => !objectDefinition.system
	);

	if (customObjectDefinitions) {
		for (const customObjecDefinition of customObjectDefinitions) {
			if (customObjecDefinition.objectRelationships.length) {
				for (const objectRelationship of customObjecDefinition.objectRelationships) {
					await request.delete(
						`${objectAdminBaseURL}/object-relationships/${objectRelationship.id}`,
						{
							headers,
						}
					);
				}
			}
		}
	}
};

export default teardown;
