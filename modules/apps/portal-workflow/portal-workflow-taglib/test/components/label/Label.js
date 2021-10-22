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

import '@testing-library/jest-dom/extend-expect';
import {render} from '@testing-library/react';
import React from 'react';

import Label from '../../../src/main/resources/META-INF/resources/workflow_status/js/components/label/Label';

describe('The Label component should', () => {
	let type, value;

	it('render with valid type and value', () => {
		type = 'id';
		value = 'Valid ID';

		const {getByText} = render(<Label type={type} value={value} />);

		const id = getByText('id');
		const idValue = getByText('Valid Id');

		expect(id).toBeTruthy();
		expect(idValue).toBeTruthy();
	});
});
