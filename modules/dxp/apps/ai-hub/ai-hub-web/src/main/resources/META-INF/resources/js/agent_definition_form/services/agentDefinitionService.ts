/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fetch} from 'frontend-js-web';

import {AgentDefinition} from '../types/AgentDefinition';

async function postAgentDefinition(agentDefinition: AgentDefinition) {
	const response = await fetch('/o/ai-hub/agent-definitions', {
		body: JSON.stringify(agentDefinition),
		headers: {
			'Content-Type': 'application/json',
		},
		method: 'POST',
	});

	return response.json();
}

export {postAgentDefinition};
