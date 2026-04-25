/**
<<<<<<< HEAD
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
=======
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
>>>>>>> a6baa77 (LPD-87444 refactor: rename ToolsUtil to ToolProviderUtil)
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util;

<<<<<<< HEAD
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
=======
import com.liferay.ai.hub.internal.assistant.tool.SitePageTools;
import com.liferay.ai.hub.internal.assistant.tool.WorkflowNodeTools;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.workflow.kaleo.definition.NodeType;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
>>>>>>> a6baa77 (LPD-87444 refactor: rename ToolsUtil to ToolProviderUtil)

/**
 * @author Feliphe Marinho
 */
public class ToolProviderUtil {

<<<<<<< HEAD
	public static List<String> getMCPServerExternalReferenceCodes(
		JSONFactory jsonFactory, Map<String, String> kaleoNodeSettingValues) {

		List<String> mcpServerExternalReferenceCodes = new ArrayList<>();

		try {
			JSONArray jsonArray = jsonFactory.createJSONArray(
				kaleoNodeSettingValues.get("tools"));

			for (JSONObject jsonObject : (Iterable<JSONObject>)jsonArray) {
				mcpServerExternalReferenceCodes.add(
					jsonObject.getString("mcpServerExternalReferenceCode"));
			}
		}
		catch (JSONException jsonException) {
			if (_log.isDebugEnabled()) {
				_log.debug(jsonException);
			}
		}

		return mcpServerExternalReferenceCodes;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ToolProviderUtil.class);
=======
	public static Object[] getTools(
		long companyId, KaleoNode currentKaleoNode,
		Map<String, Serializable> workflowContext,
		WorkflowNodeManager workflowNodeManager) {

		if (Objects.equals(
				currentKaleoNode.getType(), NodeType.AI_DECISION.name())) {

			return new Object[] {new WorkflowNodeTools(workflowNodeManager)};
		}

		if (_sitePageToolsNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {
				new SitePageTools(
					GetterUtil.getString(workflowContext.get("accessToken")),
					companyId,
					GetterUtil.getString(workflowContext.get("userToken")))
			};
		}

		return new Object[0];
	}

<<<<<<<< HEAD:modules/dxp/apps/ai-hub/ai-hub-impl/src/main/java/com/liferay/ai/hub/internal/workflow/kaleo/runtime/node/util/ToolsUtil.java
	private static final Set<String> _sitePageToolsNodeNames = Set.of(
		"pageFetcher", "pageUpdater");
========
	private static final Log _log = LogFactoryUtil.getLog(
		ToolProviderUtil.class);
>>>>>>>> a6baa77 (LPD-87444 refactor: rename ToolsUtil to ToolProviderUtil):modules/dxp/apps/ai-hub/ai-hub-impl/src/main/java/com/liferay/ai/hub/internal/workflow/kaleo/runtime/node/util/ToolProviderUtil.java
>>>>>>> a6baa77 (LPD-87444 refactor: rename ToolsUtil to ToolProviderUtil)

}