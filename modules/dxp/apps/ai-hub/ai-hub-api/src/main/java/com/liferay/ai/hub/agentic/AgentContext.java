/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.agentic;

import java.util.Map;

/**
 * @author Feliphe Marinho
 */
public class AgentContext {

	public static AgentContext.Builder builder() {
		return new AgentContext.Builder();
	}

	public AgentContext(AgentContext.Builder builder) {
		_input = builder._input;
	}

	public Map<String, Object> getInput() {
		return _input;
	}

	public static class Builder {

		public AgentContext build() {
			return new AgentContext(this);
		}

		public Builder input(Map<String, Object> input) {
			_input = input;

			return this;
		}

		private Map<String, Object> _input;

	}

	private final Map<String, Object> _input;
}
