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

import {fetch} from 'frontend-js-web';

import {HEADERS, HEADLESS_BATCH_ENGINE_URL} from './constants';

export function getImportTaskStatusURL(taskId) {
	return `${HEADLESS_BATCH_ENGINE_URL}/import-task/${taskId}`;
}

export function getImportFileURL(taskId) {
	return `${HEADLESS_BATCH_ENGINE_URL}/import-task/${taskId}/content`;
}

export async function importStatus(exportTaskId) {
	const response = await fetch(getImportTaskStatusURL(exportTaskId), {
		headers: HEADERS,
	});

	if (!response.ok) {
		throw new Error(response);
	}

	return await response.json();
}
