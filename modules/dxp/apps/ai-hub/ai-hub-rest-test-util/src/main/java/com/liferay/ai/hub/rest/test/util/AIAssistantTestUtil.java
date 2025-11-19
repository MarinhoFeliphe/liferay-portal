/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.rest.test.util;

import com.liferay.petra.string.StringBundler;

import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

/**
 * @author João Victor Alves
 */
public class AIAssistantTestUtil {

	public static String runValidatorAIModel(
		String originalText, String outputText, String prompt) {

		VertexAiGeminiChatModel vertexAiGeminiChatModel =
			VertexAiGeminiChatModel.builder(
			).project(
				""
			).location(
				"us-central1"
			).modelName(
				"gemini-2.5-flash-lite"
			).build();

		AssertionAssistant assertionAssistant = AiServices.builder(
			AssertionAssistant.class
		).systemMessageProvider(
			object -> prompt
		).chatModel(
			vertexAiGeminiChatModel
		).build();

		String responseText = null;

		try {
			responseText = assertionAssistant.assertText(
				_getUserMessage(originalText, outputText));
		}
		finally {
			vertexAiGeminiChatModel.close();
		}

		return responseText;
	}

	public interface AssertionAssistant {

		public String assertText(String text);

	}

	private static String _getUserMessage(
		String originalText, String outputText) {

		return StringBundler.concat(
			"\nThis is the ORIGINAL_TEXT: ", originalText,
			"\nThis is the OUTPUT_TEXT: ", outputText);
	}

}