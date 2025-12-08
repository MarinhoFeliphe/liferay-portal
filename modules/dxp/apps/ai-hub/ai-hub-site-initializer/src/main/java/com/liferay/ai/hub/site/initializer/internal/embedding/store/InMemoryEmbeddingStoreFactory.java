/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.embedding.store;

import com.liferay.ai.hub.embedding.store.EmbeddingStoreFactory;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

/**
 * @author Feliphe Marinho
 */
public class InMemoryEmbeddingStoreFactory implements EmbeddingStoreFactory {

	public static final String KEY = "in-memory";

	@Override
	public EmbeddingStore<TextSegment> create() {
		return new InMemoryEmbeddingStore<>();
	}

	@Override
	public String getKey() {
		return KEY;
	}

}