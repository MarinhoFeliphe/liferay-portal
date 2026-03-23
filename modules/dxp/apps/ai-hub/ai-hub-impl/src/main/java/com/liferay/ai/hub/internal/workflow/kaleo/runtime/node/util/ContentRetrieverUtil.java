/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util;

import com.liferay.ai.hub.internal.web.search.LiferayWebSearchEngine;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationKnn;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;

import java.util.Map;
import java.util.Objects;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import org.elasticsearch.client.RestClient;

/**
 * @author Feliphe Marinho
 */
public class ContentRetrieverUtil {

	public static ContentRetriever createContentRetriever(
		String accessToken, Map<String, String> kaleoNodeSettingValues,
		String userToken) {

		if (kaleoNodeSettingValues.get("rag") == null) {
			return null;
		}

		try {
			JSONObject ragJSONObject = JSONFactoryUtil.createJSONObject(
				kaleoNodeSettingValues.get("rag"));

			JSONObject contentRetrieverJSONObject = ragJSONObject.getJSONObject(
				"contentRetriever");

			if (Objects.equals(
					contentRetrieverJSONObject.getString("key"), "liferay")) {

				return WebSearchContentRetriever.builder(
				).webSearchEngine(
					new LiferayWebSearchEngine(
						accessToken,
						contentRetrieverJSONObject.getString(
							"blueprintExternalReferenceCode"),
						userToken)
				).build();
			}

			// WIP

			if (Objects.equals(
					contentRetrieverJSONObject.getString("key"),
					"foo-crawl-site")) {

				return _createElasticsearchContentRetriever();
			}
		}
		catch (JSONException jsonException) {
			if (_log.isDebugEnabled()) {
				_log.debug(jsonException);
			}
		}

		return null;
	}

	private static EmbeddingStoreContentRetriever
		_createElasticsearchContentRetriever() {

		CredentialsProvider credentialsProvider =
			new BasicCredentialsProvider();

		credentialsProvider.setCredentials(
			AuthScope.ANY,
			new UsernamePasswordCredentials("elastic", "liferay"));

		RestClient restClient = RestClient.builder(
			new HttpHost("127.0.0.1", 9201, "http")
		).setHttpClientConfigCallback(
			httpClientBuilder -> {
				httpClientBuilder.setDefaultCredentialsProvider(
					credentialsProvider);

				return httpClientBuilder;
			}
		).build();

		ElasticsearchEmbeddingStore elasticsearchEmbeddingStore =
			ElasticsearchEmbeddingStore.builder(
			).restClient(
				restClient
			).configuration(
				ElasticsearchConfigurationKnn.builder(
				).build()
			).indexName(
				"customer-1-foo-"
			).build();

		VertexAiEmbeddingModel vertexAiEmbeddingModel =
			VertexAiEmbeddingModel.builder(
			).location(
				"europe-central2"
			).modelName(
				"gemini-embedding-001"
			).project(
				"ai-hub-liferay"
			).build();

		return EmbeddingStoreContentRetriever.builder(
		).embeddingModel(
			vertexAiEmbeddingModel
		).embeddingStore(
			elasticsearchEmbeddingStore
		).build();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ContentRetrieverUtil.class);

}