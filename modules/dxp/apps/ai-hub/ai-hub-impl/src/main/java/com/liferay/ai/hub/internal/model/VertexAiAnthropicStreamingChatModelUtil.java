/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.model;

import com.liferay.ai.hub.internal.configuration.VertexAIConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;

import dev.langchain4j.model.vertexai.anthropic.VertexAiAnthropicStreamingChatModel;

/**
 * @author Feliphe Marinho
 */
public class VertexAiAnthropicStreamingChatModelUtil {

	public static VertexAiAnthropicStreamingChatModel create(long companyId)
		throws ConfigurationException {

		VertexAIConfiguration vertexAIConfiguration =
			ConfigurationProviderUtil.getCompanyConfiguration(
				VertexAIConfiguration.class, companyId);

		return VertexAiAnthropicStreamingChatModel.builder(
		).location(
			"us-central1"
		).modelName(
			"claude-opus-4-7"
		).project(
			"ai-hub-liferay"
		).build();
	}

}