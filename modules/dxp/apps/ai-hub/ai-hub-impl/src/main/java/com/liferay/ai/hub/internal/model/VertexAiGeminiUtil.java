/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.model;

import com.liferay.ai.hub.configuration.VertexAIConfiguration;
import com.liferay.ai.hub.internal.langchain4j.model.chat.listener.AIHubChatModelListenerImpl;
import com.liferay.ai.hub.quota.QuotaManager;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;

import dev.langchain4j.model.vertexai.gemini.HarmCategory;
import dev.langchain4j.model.vertexai.gemini.SafetyThreshold;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Feliphe Marinho
 */
public class VertexAiGeminiUtil {

	public static VertexAiGeminiChatModel createVertexAiGeminiChatModel(
			QuotaManager quotaManager, ServiceContext serviceContext)
		throws ConfigurationException {

		VertexAIConfiguration vertexAIConfiguration =
			ConfigurationProviderUtil.getCompanyConfiguration(
				VertexAIConfiguration.class, serviceContext.getCompanyId());

		String location = PropsUtil.get(PropsKeys.VERTEX_AI_LOCATION);

		if (Validator.isNotNull(vertexAIConfiguration.location())) {
			location = vertexAIConfiguration.location();
		}

		String modelName = PropsUtil.get(PropsKeys.VERTEX_AI_MODEL_NAME);

		if (Validator.isNotNull(vertexAIConfiguration.modelName())) {
			modelName = vertexAIConfiguration.modelName();
		}

		String projectId = PropsUtil.get(PropsKeys.VERTEX_AI_PROJECT_ID);

		if (Validator.isNotNull(vertexAIConfiguration.projectId())) {
			projectId = vertexAIConfiguration.projectId();
		}

		VertexAiGeminiChatModel.VertexAiGeminiChatModelBuilder builder =
			VertexAiGeminiChatModel.builder();

		if (Objects.equals(location, "global")) {
			builder.apiEndpoint("aiplatform.googleapis.com");
		}

		return builder.listeners(
			List.of(
				new AIHubChatModelListenerImpl(quotaManager, serviceContext))
		).location(
			location
		).modelName(
			modelName
		).project(
			projectId
		).safetySettings(
			_safetyThresholds
		).build();
	}

	public static VertexAiGeminiStreamingChatModel
			createVertexAiGeminiStreamingChatModel(
				QuotaManager quotaManager, ServiceContext serviceContext)
		throws ConfigurationException {

		VertexAIConfiguration vertexAIConfiguration =
			ConfigurationProviderUtil.getCompanyConfiguration(
				VertexAIConfiguration.class, serviceContext.getCompanyId());

		String location = PropsUtil.get(PropsKeys.VERTEX_AI_LOCATION);

		if (Validator.isNotNull(vertexAIConfiguration.location())) {
			location = vertexAIConfiguration.location();
		}

		String modelName = PropsUtil.get(PropsKeys.VERTEX_AI_MODEL_NAME);

		if (Validator.isNotNull(vertexAIConfiguration.modelName())) {
			modelName = vertexAIConfiguration.modelName();
		}

		String projectId = PropsUtil.get(PropsKeys.VERTEX_AI_PROJECT_ID);

		if (Validator.isNotNull(vertexAIConfiguration.projectId())) {
			projectId = vertexAIConfiguration.projectId();
		}

		VertexAiGeminiStreamingChatModel.VertexAiGeminiStreamingChatModelBuilder
			builder = VertexAiGeminiStreamingChatModel.builder();

		if (Objects.equals(location, "global")) {
			builder.apiEndpoint("aiplatform.googleapis.com");
		}

		return builder.listeners(
			List.of(
				new AIHubChatModelListenerImpl(quotaManager, serviceContext))
		).location(
			location
		).modelName(
			modelName
		).project(
			projectId
		).safetySettings(
			_safetyThresholds
		).build();
	}

	private static final Map<HarmCategory, SafetyThreshold> _safetyThresholds =
		Map.of(
			HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
			SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE,
			HarmCategory.HARM_CATEGORY_HARASSMENT,
			SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE,
			HarmCategory.HARM_CATEGORY_HATE_SPEECH,
			SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE,
			HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT,
			SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE);

}