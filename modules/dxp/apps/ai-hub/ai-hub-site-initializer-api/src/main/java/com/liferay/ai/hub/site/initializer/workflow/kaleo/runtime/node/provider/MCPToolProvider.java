/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.workflow.kaleo.runtime.node.provider;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;

import dev.langchain4j.mcp.McpToolProvider;

/**
 * @author João Victor Alves
 */
public interface MCPToolProvider {

	public McpToolProvider provide(
			KaleoInstanceToken kaleoInstanceToken, String tools)
		throws PortalException;

}