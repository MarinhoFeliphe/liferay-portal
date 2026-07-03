/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.langchain4j.model.chat;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.ChatRequestOptions;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;
import java.util.Set;

/**
 * @author Iliyan Peychev
 */
public class PromptResponseFormatChatModel implements ChatModel {

	public PromptResponseFormatChatModel(ChatModel chatModel) {
		_chatModel = chatModel;
	}

	@Override
	public ChatResponse chat(
		ChatRequest chatRequest, ChatRequestOptions chatRequestOptions) {

		return _chatModel.chat(chatRequest, chatRequestOptions);
	}

	@Override
	public ChatRequestParameters defaultRequestParameters() {
		return _chatModel.defaultRequestParameters();
	}

	@Override
	public List<ChatModelListener> listeners() {
		return _chatModel.listeners();
	}

	@Override
	public ModelProvider provider() {
		return _chatModel.provider();
	}

	@Override
	public Set<Capability> supportedCapabilities() {
		return Set.of();
	}

	private final ChatModel _chatModel;

}