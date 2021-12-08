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

import {parseCSV} from '../../src/main/resources/META-INF/resources/js/FileParsers';

const fileContents = `currencyCode,type,name
    USD,site,My Channel 0
    USD,site,My Channel 1
    USD,site,My Channel 2
    USD,site,My Channel 3
    USD,site,My Channel 4
`;
const fileSchema = ['currencyCode', 'type', 'name'];
const file = new Blob([fileContents], {type: 'text/csv'});
const readAsText = jest.fn();
let dummyFileReader;

const onComplete = jest.fn();
const onError = jest.fn();

function mockFileReader(addEventListener) {
	dummyFileReader = {
		addEventListener,
		loaded: false,
		readAsText,
		result: fileContents,
	};
	window.FileReader = jest.fn(() => dummyFileReader);
}

describe('FileParsers', () => {
	beforeEach(() => {
		jest.clearAllMocks();
	});

	it('must correctly call onError when columns not detected', () => {
		const onProgressEvent = {
			target: {
				result: `currencyCode,ty`,
			},
		};

		mockFileReader(
			jest.fn((_, evtHandler) => {
				evtHandler(onProgressEvent);
			})
		);

		parseCSV({
			file,
			onComplete,
			onError,
		});

		expect(onComplete).not.toBeCalledWith(fileSchema);
		expect(onError).toBeCalled();
	});

	it('must correctly call onComplete', () => {
		const onProgressEvent = {
			target: {
				result: `currencyCode,type,name
					USD,site,My Channel 0`,
			},
		};

		mockFileReader(
			jest.fn((_, evtHandler) => {
				evtHandler(onProgressEvent);
			})
		);

		parseCSV({
			file,
			onComplete,
			onError,
		});

		expect(onComplete).toBeCalledWith(fileSchema);
		expect(onError).not.toBeCalled();
	});
});
