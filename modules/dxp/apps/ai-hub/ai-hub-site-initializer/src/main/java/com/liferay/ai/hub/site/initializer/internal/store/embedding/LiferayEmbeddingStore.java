/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.store.embedding;

import com.liferay.ai.hub.site.initializer.internal.AIHubSiteInitializer;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.search.experiences.rest.dto.v1_0.DocumentField;
import com.liferay.search.experiences.rest.dto.v1_0.Hit;
import com.liferay.search.experiences.rest.dto.v1_0.SearchHits;
import com.liferay.search.experiences.rest.dto.v1_0.SearchResponse;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Feliphe Marinho
 */
public class LiferayEmbeddingStore implements EmbeddingStore<TextSegment> {

	@Override
	public String add(Embedding embedding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String add(Embedding embedding, TextSegment textSegment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(String id, Embedding embedding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> addAll(List<Embedding> list) {
		throw new UnsupportedOperationException();
	}

	public EmbeddingSearchResult<TextSegment> search(String text) {
		SearchResponse searchResponse = _search(text);

		if (searchResponse == null) {
			return null;
		}

		List<EmbeddingMatch<TextSegment>> embeddingMatches = new ArrayList<>();

		SearchHits searchHits = searchResponse.getSearchHits();

		for (Hit hit : searchHits.getHits()) {
			Map<String, DocumentField> documentFields = hit.getDocumentFields();

			DocumentField textEmbeddedDocumentField =
				documentFields.get("text_embedded_en_US");
			DocumentField textEmbeddingDocumentField =
				documentFields.get("text_embedding_3072_en_US");

			Float score = hit.getScore();

			embeddingMatches.add(
				new EmbeddingMatch<>(
					score.doubleValue(), hit.getId(),
					new Embedding(
						TransformUtil.transformToFloatArray(
							textEmbeddingDocumentField.getValues(),
							value -> Float.parseFloat((String)value))),
					TextSegment.from(
						String.valueOf(
							textEmbeddedDocumentField.getValues()[0]))));
		}

		return new EmbeddingSearchResult<>(embeddingMatches);
	}

	@Override
	public EmbeddingSearchResult<TextSegment> search(
		EmbeddingSearchRequest embeddingSearchRequest) {

		throw new UnsupportedOperationException();
	}

	private String _getAuthorization() {
		try {
			Http.Options options = new Http.Options();

			options.setLocation("http://localhost:8080/o/oauth2/token");
			options.setMethod(Http.Method.POST);

			options.addPart("client_id",
				"id-6d679918-5427-9f8c-eae1-798f76f49d57");
			options.addPart("client_secret",
				"secret-29ed104d-69cb-107f-59aa-6814684a7de");
			options.addPart("grant_type", "client_credentials");

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				HttpUtil.URLtoString(options));

			return "Bearer " + jsonObject.getString("access_token");
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private SearchResponse _search(String query) {
		try {
			Http.Options options = new Http.Options();

			options.setBody(
				StringUtil.read(
					AIHubSiteInitializer.class.getResourceAsStream(
						"dependencies/body.json")),
				ContentTypes.APPLICATION_JSON, StringPool.UTF8);

			options.addHeader(HttpHeaders.AUTHORIZATION, _getAuthorization());
			options.addHeader(
				HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);

			options.setLocation(
				HttpComponentsUtil.addParameter(
					"http://localhost:8080/o/search-experiences-rest/v1.0/" +
					"search", "query", query));

			options.setMethod(Http.Method.POST);

			return SearchResponse.toDTO(HttpUtil.URLtoString(options));
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LiferayEmbeddingStore.class);

}