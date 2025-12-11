/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.assistant.handler;

import com.liferay.ai.hub.site.initializer.internal.rag.content.retriever.LiferayEmbeddingStoreContentRetriever;
import com.liferay.ai.hub.site.initializer.internal.store.embedding.LiferayEmbeddingStore;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;

/**
 * @author Feliphe Marinho
 */
public class ContentRetrieverUtil {

	public static ContentRetriever createContentRetriever() {
		return new LiferayEmbeddingStoreContentRetriever(
			VertexAiEmbeddingModel.builder(
			).location(
				"us-central1"
			).modelName(
				"gemini-embedding-001"
			).project(
				"ai-hub-liferay"
			).publisher(
				"google"
			).build(),
			new LiferayEmbeddingStore());
	}

}