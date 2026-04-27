/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.tools;

import com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util.ToolsConfigUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Feliphe Marinho
 */
public class ToolsUtil {

	public static Object[] getTools(
		JSONFactory jsonFactory, Map<String, String> kaleoNodeSettingValues,
		WorkflowNodeManager workflowNodeManager) {

		List<Object> tools = new ArrayList<>();

		for (String name :
				ToolsConfigUtil.getValues(
					jsonFactory, kaleoNodeSettingValues, "name")) {

			if (Objects.equals(name, "cmsBlog")) {
				tools.add(new CMSBlogTools());
			}
			else if (Objects.equals(name, "workflowNode")) {
				tools.add(new WorkflowNodeTools(workflowNodeManager));
			}
		}

		return tools.toArray();
	}

}