/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author João Victor Alves
 */
@ExtendedObjectClassDefinition(
	category = "mcp-server", scope = ExtendedObjectClassDefinition.Scope.GROUP
)
@Meta.OCD(
	id = "com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.configuration.MCPConfiguration",
	localization = "content/Language", name = "mcp-server-configuration-name"
)
public interface MCPConfiguration {

	@Meta.AD(deflt = "false", name = "enabled", required = false)
	public boolean enabled();

	@Meta.AD(name = "sse-url", required = false)
	public String sseUrl();

	@Meta.AD(
		deflt = "basic", description = "authorization-type",
		name = "authorization-type", optionLabels = "%basic",
		optionValues = "basic", required = false
	)
	public String authorizationType();

	@Meta.AD(name = "userName", required = false)
	public String userName();

	@Meta.AD(name = "password", required = false, type = Meta.Type.Password)
	public String password();

}