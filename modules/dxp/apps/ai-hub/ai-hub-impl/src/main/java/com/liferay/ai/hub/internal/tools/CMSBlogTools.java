/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.tools;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalServiceUtil;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.invocation.InvocationParameters;

import java.io.Serializable;

import java.util.Map;

/**
 * @author Feliphe Marinho
 */
public class CMSBlogTools {

	@Tool(
		"Add a single CMSBlog item to the current chat's pending import. Returns the saved item's externalReferenceCode."
	)
	public String addItem(
			InvocationParameters invocationParameters,
			@P("A string containing the JSON of a single CMSBlog DTO to import")
				String item)
		throws Exception {

		ExecutionContext executionContext = invocationParameters.get(
			"executionContext");

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		String accessToken = GetterUtil.getString(
			workflowContext.get("accessToken"));

		CompanyThreadLocal.setCompanyId(
			executionContext.getServiceContext(
			).getCompanyId());

		OAuth2Authorization oAuth2Authorization =
			OAuth2AuthorizationLocalServiceUtil.
				getOAuth2AuthorizationByAccessTokenContent(
					accessToken.substring(7));

		OAuth2Application oAuth2Application =
			OAuth2ApplicationLocalServiceUtil.getOAuth2Application(
				oAuth2Authorization.getOAuth2ApplicationId());

		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader(
			"Liferay-AI-Hub-Cell-On-Behalf-Of",
			GetterUtil.getString(workflowContext.get("userToken")));
		options.setBody(
			JSONUtil.put(
				"json", item
			).put(
				"r_compositionToArtifacts_contentComposerCompositionERC",
				"0cfe4050-40ca-3cdb-f042-83e1e207ef67"
			).toString(),
			ContentTypes.APPLICATION_JSON, "UTF-8");
		options.setLocation(
			oAuth2Application.getHomePageURL() +
				"/o/content-composer/artifacts");
		options.setMethod(Http.Method.POST);

		JSONObject responseJSONObject = JSONFactoryUtil.createJSONObject(
			HttpUtil.URLtoString(options));

		return responseJSONObject.getString("externalReferenceCode");
	}

}