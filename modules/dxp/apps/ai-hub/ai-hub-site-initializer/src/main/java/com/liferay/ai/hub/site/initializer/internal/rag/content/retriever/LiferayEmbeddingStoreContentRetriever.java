/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.rag.content.retriever;

import com.liferay.ai.hub.site.initializer.internal.store.embedding.LiferayEmbeddingStore;
import com.liferay.petra.function.transform.TransformUtil;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;

import java.util.List;
import java.util.Map;

/**
 * @author Feliphe Marinho
 */
public class LiferayEmbeddingStoreContentRetriever extends
	EmbeddingStoreContentRetriever {

	private final LiferayEmbeddingStore _liferayEmbeddingStore;

	public LiferayEmbeddingStoreContentRetriever(
		EmbeddingModel embeddingModel, LiferayEmbeddingStore liferayEmbeddingStore) {

		super(liferayEmbeddingStore, embeddingModel);

		_liferayEmbeddingStore = liferayEmbeddingStore;
	}

	@Override
	public List<Content> retrieve(Query query) {
		EmbeddingSearchResult<TextSegment> searchResult =
			_liferayEmbeddingStore.search(query.text());

		return TransformUtil.transform(
			searchResult.matches(),
			embeddingMatch ->
				Content.from(
					embeddingMatch.embedded(),
					Map.of(
						ContentMetadata.SCORE, embeddingMatch.score(),
						ContentMetadata.EMBEDDING_ID,
						embeddingMatch.embeddingId())));
	}
}
