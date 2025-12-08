/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.embedding.store;

import com.liferay.ai.hub.embedding.store.EmbeddingStoreFactory;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnection;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionManager;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;

import org.elasticsearch.client.RestHighLevelClient;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(service = EmbeddingStoreFactory.class)
public class ElasticsearchEmbeddingStoreFactory
	implements EmbeddingStoreFactory {

	@Override
	public EmbeddingStore<TextSegment> create() {
		ElasticsearchConnection elasticsearchConnection =
			_elasticsearchConnectionManager.getElasticsearchConnection();

		RestHighLevelClient restHighLevelClient =
			elasticsearchConnection.getRestHighLevelClient();

		return ElasticsearchEmbeddingStore.builder(
		).indexName(
			"foo-bar"
		).restClient(
			restHighLevelClient.getLowLevelClient()
		).build();
	}

	@Override
	public String getKey() {
		return "elasticsearch7";
	}

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

}