/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.util;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author João Victor Alves
 */
public class MCPToolProviderUtil {

	public static McpToolProvider provide(
			DTOConverterRegistry dtoConverterRegistry,
			GroupLocalService groupLocalService,
			KaleoInstanceToken kaleoInstanceToken,
			ObjectDefinitionLocalService objectDefinitionLocalService,
			ObjectEntryManager objectEntryManager, String tools,
			UserService userService,
			WorkflowInstanceManager workflowInstanceManager)
		throws PortalException {

		JSONArray toolsJSONArray = JSONFactoryUtil.createJSONArray(tools);

		Iterator<JSONObject> iterator = toolsJSONArray.iterator();

		List<String> externalReferenceCodes = new ArrayList<>();

		while (iterator.hasNext()) {
			JSONObject jsonObject = iterator.next();

			externalReferenceCodes.add(
				jsonObject.getString("externalReferenceCode"));
		}

		Page<ObjectEntry> page = null;

		WorkflowInstance workflowInstance =
			workflowInstanceManager.getWorkflowInstance(
				CompanyThreadLocal.getCompanyId(),
				kaleoInstanceToken.getKaleoInstanceId());

		Group group = groupLocalService.fetchGroup(
			workflowInstance.getGroupId());

		try {
			page = objectEntryManager.getObjectEntries(
				CompanyThreadLocal.getCompanyId(),
				objectDefinitionLocalService.
					fetchObjectDefinitionByExternalReferenceCode(
						"L_MCP_SERVER", CompanyThreadLocal.getCompanyId()),
				group.getGroupKey(), null,
				new DefaultDTOConverterContext(
					false, Collections.emptyMap(), dtoConverterRegistry, null,
					LocaleUtil.getDefault(), null,
					userService.getUserById(kaleoInstanceToken.getUserId())),
				"externalReferenceCode in ('" +
					String.join("', '", externalReferenceCodes) + "')",
				null, null, null);
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		McpToolProvider toolProvider = null;

		if (page != null) {
			List<ObjectEntry> objectEntries =
				(List<ObjectEntry>)page.getItems();

			List<McpClient> mcpClients = new ArrayList<>();

			for (ObjectEntry objectEntry : objectEntries) {
				Map<String, Object> objectEntryProperties =
					objectEntry.getProperties();

				McpTransport mcpTransport = null;

				if (Objects.equals(
						objectEntry.getExternalReferenceCode(),
						"L_LIFERAY_MCP_SERVER")) {

					mcpTransport = new HttpMcpTransport.Builder(
					).sseUrl(
						GetterUtil.getString(objectEntryProperties.get("url"))
					).customHeaders(
						Map.of(
							"Authorization",
							_getAuthorization(
								GetterUtil.getString(
									objectEntryProperties.get("credentials"))))
					).build();
				}
				else {
					mcpTransport = new StreamableHttpMcpTransport.Builder(
					).url(
						GetterUtil.getString(objectEntryProperties.get("url"))
					).customHeaders(
						Map.of(
							"Authorization",
							_getAuthorization(
								GetterUtil.getString(
									objectEntryProperties.get("credentials"))))
					).build();
				}

				McpClient mcpClient = new DefaultMcpClient.Builder(
				).transport(
					mcpTransport
				).build();

				mcpClients.add(mcpClient);
			}

			toolProvider = McpToolProvider.builder(
			).mcpClients(
				mcpClients
			).build();
		}

		return toolProvider;
	}

	private static String _getAuthorization(String credentials) {
		try {
			Base64.Encoder encoder = Base64.getEncoder();

			return "Basic " +
				new String(
					encoder.encode(credentials.getBytes("UTF-8")), "UTF-8");
		}
		catch (UnsupportedEncodingException unsupportedEncodingException) {
			throw new RuntimeException(unsupportedEncodingException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MCPToolProviderUtil.class);

}