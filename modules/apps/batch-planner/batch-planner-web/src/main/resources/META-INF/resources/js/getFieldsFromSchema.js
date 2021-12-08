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

const getFieldsFromSchema = (schema) => {
	const dbFields = [];

	for (const [label, property] of Object.entries(schema)) {
		if (property.writeOnly || label.startsWith('x-')) {
			continue;
		}

		let value = label;

		if (property.extensions && property.extensions['x-parent-map']) {
			value = property.extensions['x-parent-map'] + '_' + label;
		}

		const field = {label, value};

		dbFields.push(field);
	}

	return dbFields;
};

export default getFieldsFromSchema;
