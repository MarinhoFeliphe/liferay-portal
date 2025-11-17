/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.configuration.manager.util;

import com.liferay.ai.hub.site.initializer.internal.workflow.kaleo.runtime.node.configuration.MCPConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;

/**
 * @author João Victor Alves
 */
public class MCPConfigurationManagerUtil {

	public static String authorizationType(
		ConfigurationProvider configurationProvider, long groupId) {

		try {
			MCPConfiguration mcpConfiguration =
				configurationProvider.getGroupConfiguration(
					MCPConfiguration.class, groupId);

			return mcpConfiguration.authorizationType();
		}
		catch (ConfigurationException configurationException) {
			throw new RuntimeException(configurationException);
		}
	}

	public static boolean enabled(
		ConfigurationProvider configurationProvider, long groupId) {

		try {
			MCPConfiguration mcpConfiguration =
				configurationProvider.getGroupConfiguration(
					MCPConfiguration.class, groupId);

			return mcpConfiguration.enabled();
		}
		catch (ConfigurationException configurationException) {
			throw new RuntimeException(configurationException);
		}
	}

	public static String password(
		ConfigurationProvider configurationProvider, long groupId) {

		try {
			MCPConfiguration mcpConfiguration =
				configurationProvider.getGroupConfiguration(
					MCPConfiguration.class, groupId);

			return mcpConfiguration.password();
		}
		catch (ConfigurationException configurationException) {
			throw new RuntimeException(configurationException);
		}
	}

	public static String sseUrl(
		ConfigurationProvider configurationProvider, long groupId) {

		try {
			MCPConfiguration mcpConfiguration =
				configurationProvider.getGroupConfiguration(
					MCPConfiguration.class, groupId);

			return mcpConfiguration.sseUrl();
		}
		catch (ConfigurationException configurationException) {
			throw new RuntimeException(configurationException);
		}
	}

	public static String userName(
		ConfigurationProvider configurationProvider, long groupId) {

		try {
			MCPConfiguration mcpConfiguration =
				configurationProvider.getGroupConfiguration(
					MCPConfiguration.class, groupId);

			return mcpConfiguration.userName();
		}
		catch (ConfigurationException configurationException) {
			throw new RuntimeException(configurationException);
		}
	}

}