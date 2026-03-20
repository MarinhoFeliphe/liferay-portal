/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util;

import com.liferay.ai.hub.internal.web.search.LiferayWebSearchEngine;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.UserServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author Feliphe Marinho
 */
public class ContentRetrieverUtil {

	public static ContentRetriever createContentRetriever(
		String accessToken, long companyId,
		DTOConverterRegistry dtoConverterRegistry,
		Map<String, String> kaleoNodeSettingValues, Locale locale,
		ObjectEntryManager objectEntryManager,
		SearchEngineAdapter searchEngineAdapter, String userToken,
		long userId) {

		String[] indexNames = TransformUtil.transformToArray(
			_getCrawlTargetObjectEntries(
				companyId, dtoConverterRegistry, locale, objectEntryManager,
				userId),
			crawlTargetEntry -> GetterUtil.getString(
				crawlTargetEntry.getPropertyValue("indexName")),
			String.class);

		if (indexNames.length > 0) {
			return query -> _search(indexNames, query, searchEngineAdapter);
		}

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
		}
		catch (JSONException jsonException) {
			if (_log.isDebugEnabled()) {
				_log.debug(jsonException);
			}
		}

		return null;
	}

	private static List<ObjectEntry> _getCrawlTargetObjectEntries(
		long companyId, DTOConverterRegistry dtoConverterRegistry,
		Locale locale, ObjectEntryManager objectEntryManager, long userId) {

		try {
			Page<ObjectEntry> page = objectEntryManager.getObjectEntries(
				companyId,
				ObjectDefinitionLocalServiceUtil.
					fetchObjectDefinitionByExternalReferenceCode(
						"L_AI_HUB_CRAWL_TARGET", companyId),
				null, null,
				new DefaultDTOConverterContext(
					false, Map.of(), dtoConverterRegistry, null, locale, null,
					UserServiceUtil.getUserById(userId)),
				null, null, null, null);

			return (List<ObjectEntry>)page.getItems();
		}
		catch (Exception exception) {
			_log.error(exception);

			return List.of();
		}
	}

	private static List<Content> _search(
		String[] indexNames, Query query,
		SearchEngineAdapter searchEngineAdapter) {

		VertexAiEmbeddingModel vertexAiEmbeddingModel =
			VertexAiEmbeddingModel.builder(
			).location(
				"europe-central2"
			).modelName(
				"gemini-embedding-001"
			).project(
				"ai-hub-liferay"
			).publisher(
				"google"
			).build();

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
		).put(
			"knn",
			JSONFactoryUtil.createJSONObject(
			).put(
				"field", "text_embedding_3072"
			).put(
				"k", 10
			).put(
				"num_candidates", 100
			).put(
				"query_vector",
				() -> {
					Embedding embedding = vertexAiEmbeddingModel.embed(
						TextSegment.from(query.text())
					).content();

					return JSONFactoryUtil.createJSONArray(
						embedding.vectorAsList());
				}
			)
		);

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setFetchSourceIncludes(new String[] {"text"});
		searchSearchRequest.setIndexNames(indexNames);
		searchSearchRequest.setQuery(
			QueriesUtil.wrapper(jsonObject.toString()));
		searchSearchRequest.setSize(10);

		SearchSearchResponse searchSearchResponse = searchEngineAdapter.execute(
			searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		return TransformUtil.transform(
			searchHits.getSearchHits(),
			searchHit -> Content.from(
				MapUtil.getString(searchHit.getSourcesMap(), "objectEntryContent")));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ContentRetrieverUtil.class);

}