/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.provider;

import com.liferay.ai.hub.site.initializer.workflow.kaleo.runtime.node.provider.MCPToolProvider;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author João Victor Alves
 */
@Component(service = MCPToolProvider.class)
public class MCPToolProviderImpl implements MCPToolProvider {

	public McpToolProvider provide(
			KaleoInstanceToken kaleoInstanceToken, String tools)
		throws PortalException {

		Page<ObjectEntry> page = null;

		WorkflowInstance workflowInstance =
			_workflowInstanceManager.getWorkflowInstance(
				kaleoInstanceToken.getCompanyId(),
				kaleoInstanceToken.getKaleoInstanceId());

		Group group = _groupLocalService.fetchGroup(
			workflowInstance.getGroupId());

		try {
			page = _objectEntryManager.getObjectEntries(
				kaleoInstanceToken.getCompanyId(),
				_objectDefinitionLocalService.
					fetchObjectDefinitionByExternalReferenceCode(
						"L_MCP_SERVER", kaleoInstanceToken.getCompanyId()),
				group.getGroupKey(), null,
				new DefaultDTOConverterContext(
					false, Collections.emptyMap(), _dtoConverterRegistry, null,
					LocaleUtil.getDefault(), null,
					_userService.getUserById(kaleoInstanceToken.getUserId())),
				_getFilterString(tools), null, null, null);
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		if (page == null) {
			return null;
		}

		List<ObjectEntry> objectEntries = (List<ObjectEntry>)page.getItems();

		List<McpClient> mcpClients = new ArrayList<>();

		for (ObjectEntry objectEntry : objectEntries) {
			McpTransport mcpTransport = _createMCPTransport(
				objectEntry.getExternalReferenceCode(),
				objectEntry.getProperties());

			McpClient mcpClient = new DefaultMcpClient.Builder(
			).transport(
				mcpTransport
			).build();

			mcpClients.add(mcpClient);
		}

		return McpToolProvider.builder(
		).mcpClients(
			mcpClients
		).build();
	}

	private McpTransport _createMCPTransport(
		String externalReferenceCode, Map<String, Object> properties) {

		if (Objects.equals(externalReferenceCode, "L_LIFERAY_MCP_SERVER")) {
			return new HttpMcpTransport.Builder(
			).sseUrl(
				GetterUtil.getString(properties.get("url"))
			).customHeaders(
				Map.of(
					"Authorization",
					_getAuthorization(
						GetterUtil.getString(properties.get("credentials"))))
			).build();
		}

		return new StreamableHttpMcpTransport.Builder(
		).url(
			GetterUtil.getString(properties.get("url"))
		).customHeaders(
			Map.of(
				"Authorization",
				_getAuthorization(
					GetterUtil.getString(properties.get("credentials"))))
		).build();
	}

	private String _getAuthorization(String credentials) {
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

	private String _getFilterString(String tools) throws Exception {
		List<String> valuesList = new ArrayList<>();

		for (Object value : _jsonFactory.createJSONArray(tools)) {
			valuesList.add(StringUtil.quote(String.valueOf(value)));
		}

		return StringBundler.concat(
			"externalReferenceCode in (",
			StringUtil.merge(valuesList, StringPool.COMMA_AND_SPACE), ")");
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MCPToolProviderImpl.class);

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference(
		target = "(object.entry.manager.storage.type=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private ObjectEntryManager _objectEntryManager;

	@Reference
	private UserService _userService;

	@Reference
	private WorkflowInstanceManager _workflowInstanceManager;

}