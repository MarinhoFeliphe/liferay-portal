/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.assistant.tool;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalServiceUtil;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalServiceUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Feliphe Marinho
 */
public class PageSpecificationTools {

	public PageSpecificationTools(
		String accessToken, long companyId, String userToken) {

		_accessToken = accessToken;
		_companyId = companyId;
		_userToken = userToken;
	}

	@Tool(
		"Retrieve a single page specification (draft or published) of a " +
			"site page by its external reference code."
	)
	public String getPageSpecification(
		@P("Site external reference code") String siteExternalReferenceCode,
		@P("Page specification external reference code") String
			pageSpecificationExternalReferenceCode) {

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_companyId)) {

			return _getPageSpecification(
				pageSpecificationExternalReferenceCode,
				siteExternalReferenceCode);
		}
		catch (Exception exception) {
			return ReflectionUtil.throwException(exception);
		}
	}

	@Tool(
		"Update the draft page specification of a site page. The body must " +
			"be the full ContentPageSpecification JSON payload for the " +
				"draft; the draft layout is replaced wholesale and the " +
					"published version is left untouched."
	)
	public String updatePageSpecification(
		@P("Site external reference code") String siteExternalReferenceCode,
		@P("Page specification external reference code") String
			pageSpecificationExternalReferenceCode,
		@P("Full ContentPageSpecification JSON payload for the draft")
			String body) {

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_companyId)) {

			return _updatePageSpecification(
				body, pageSpecificationExternalReferenceCode,
				siteExternalReferenceCode);
		}
		catch (Exception exception) {
			return ReflectionUtil.throwException(exception);
		}
	}

	private String _getHomePageURL() throws Exception {
		if (Validator.isNull(_accessToken) ||
			!_accessToken.startsWith("Bearer ")) {

			throw new IllegalArgumentException();
		}

		OAuth2Authorization oAuth2Authorization =
			OAuth2AuthorizationLocalServiceUtil.
				getOAuth2AuthorizationByAccessTokenContent(
					_accessToken.substring(7));

		OAuth2Application oAuth2Application =
			OAuth2ApplicationLocalServiceUtil.getOAuth2Application(
				oAuth2Authorization.getOAuth2ApplicationId());

		return oAuth2Application.getHomePageURL();
	}

	private String _getPageSpecification(
			String pageSpecificationExternalReferenceCode,
			String siteExternalReferenceCode)
		throws Exception {

		String location = _getPageSpecificationLocation(
			pageSpecificationExternalReferenceCode, siteExternalReferenceCode);

		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader("Liferay-AI-Hub-Cell-On-Behalf-Of", _userToken);
		options.setLocation(location);
		options.setMethod(Http.Method.GET);

		String responseBody = HttpUtil.URLtoString(options);

		int responseCode = options.getResponse(
		).getResponseCode();

		if ((responseCode < 200) || (responseCode >= 300)) {
			return responseBody;
		}

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(responseBody);

		_pruneReadOnlyFields(jsonObject);

		return jsonObject.toString();
	}

	private String _getPageSpecificationLocation(
			String pageSpecificationExternalReferenceCode,
			String siteExternalReferenceCode)
		throws Exception {

		return StringBundler.concat(
			_getHomePageURL(), "/o/headless-admin-site/v1.0/sites/",
			URLCodec.encodeURL(siteExternalReferenceCode),
			"/page-specifications/",
			URLCodec.encodeURL(pageSpecificationExternalReferenceCode));
	}

	private void _pruneReadOnlyFields(Object value) {
		if (value instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject)value;

			Set<String> readOnlyKeys = Set.of(
				"configuration", "css", "customFields", "datePropagated",
				"draftFragmentInstanceExternalReferenceCode", "html", "indexed",
				"js", "namespace", "pageSpecificationExternalReferenceCode",
				"taxonomyCategoryBriefs", "uuid");

			for (String key : readOnlyKeys) {
				jsonObject.remove(key);
			}

			for (String key : new HashSet<>(jsonObject.keySet())) {
				_pruneReadOnlyFields(jsonObject.get(key));
			}
		}
		else if (value instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray)value;

			for (int i = 0; i < jsonArray.length(); i++) {
				_pruneReadOnlyFields(jsonArray.get(i));
			}
		}
	}

	private String _stripMarkdownFences(String body) {
		if (body == null) {
			return body;
		}

		body = body.trim();

		if (body.startsWith("```")) {
			int firstNewline = body.indexOf('\n');

			if (firstNewline > 0) {
				body = body.substring(firstNewline + 1);
			}
			else {
				body = body.substring(3);
			}
		}

		if (body.endsWith("```")) {
			body = body.substring(0, body.length() - 3);
		}

		return body.trim();
	}

	private String _updatePageSpecification(
			String body, String pageSpecificationExternalReferenceCode,
			String siteExternalReferenceCode)
		throws Exception {

		body = _stripMarkdownFences(body);

		String location = _getPageSpecificationLocation(
			pageSpecificationExternalReferenceCode, siteExternalReferenceCode);

		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader("Liferay-AI-Hub-Cell-On-Behalf-Of", _userToken);
		options.setBody(body, ContentTypes.APPLICATION_JSON, "UTF-8");
		options.setLocation(location);
		options.setMethod(Http.Method.PUT);

		String responseBody = HttpUtil.URLtoString(options);

		Http.Response response = options.getResponse();

		int responseCode = response.getResponseCode();

		if ((responseCode < 200) || (responseCode >= 300)) {
			_log.error(
				StringBundler.concat(
					"updatePageSpecification failed with HTTP ", responseCode,
					" for site ", siteExternalReferenceCode,
					" and page specification ",
					pageSpecificationExternalReferenceCode, ". Request body: ",
					body, ". Response body: ", responseBody));

			return StringBundler.concat(
				"HTTP ", responseCode,
				". The server rejected the request. Compare the body you ",
				"sent with the error response below, correct the body, and ",
				"call updatePageSpecification again.\n\nRequest body sent:\n",
				body, "\n\nError response:\n", responseBody);
		}

		return responseBody;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PageSpecificationTools.class);

	private final String _accessToken;
	private final long _companyId;
	private final String _userToken;

}
