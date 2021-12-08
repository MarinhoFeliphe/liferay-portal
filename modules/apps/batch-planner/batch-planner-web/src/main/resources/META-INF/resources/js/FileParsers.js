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

import {PARSE_FILE_CHUNK_SIZE} from './constants';

function extractFieldsFromCSV(content, fieldSeparator) {
	if (content.indexOf('\n') > -1) {
		const splitLines = content.split('\n');

		const firstNoEmptyLine = splitLines.find((line) => line.length > 0);

		return firstNoEmptyLine.split(fieldSeparator);
	}
}

export function parseCSV({fieldSeparator = ',', file, onComplete, onError}) {
	let abort = false;
	const fileSize = file.size;
	let offset = 0;

	const chunkReaderBlock = (_offset, length, _file) => {
		const reader = new FileReader();
		const blob = _file.slice(_offset, length + _offset);
		reader.addEventListener('load', readEventHandler);
		reader.readAsText(blob);
	};

	const readEventHandler = (event) => {
		if (event.target.error || abort) {
			return onError();
		}

		offset += event.target.result.length;

		const fields = extractFieldsFromCSV(
			event.target.result,
			fieldSeparator
		);

		if (fields) {
			return onComplete(fields);
		}
		else if (offset >= fileSize) {
			return onError();
		}

		chunkReaderBlock(offset, PARSE_FILE_CHUNK_SIZE, file);
	};

	chunkReaderBlock(offset, PARSE_FILE_CHUNK_SIZE, file);

	return () => {
		abort = true;
	};
}
