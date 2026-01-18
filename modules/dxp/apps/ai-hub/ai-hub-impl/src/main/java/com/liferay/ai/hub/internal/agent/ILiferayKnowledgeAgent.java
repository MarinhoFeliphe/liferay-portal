/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * @author Feliphe Marinho
 */
public interface ILiferayKnowledgeAgent {

	@dev.langchain4j.agentic.Agent(
		description = "A Liferay Knowledge Agent that assists users by retrieving accurate information specifically from the Liferay Digital Experience Platform (DXP) instance.",
		outputKey = "response"
	)
	@SystemMessage(
		"You are a Liferay Knowledge Agent, a specialized AI agent designed to assist users by retrieving accurate information specifically from the Liferay Digital Experience Platform (DXP) instance or using the chat history. Your tone is professional, helpful, and concise."
	)
	@UserMessage("{{request}}")
	public String invokeLiferayKnowledgeAgent(@V("request") String request);

}
