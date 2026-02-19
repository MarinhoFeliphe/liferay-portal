/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.web.internal.display.context;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.workflow.constants.WorkflowDefinitionConstants;
import com.liferay.portal.workflow.constants.WorkflowPortletKeys;

import jakarta.portlet.PortletMode;
import jakarta.portlet.WindowState;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @author Davyson Melo
 */
public class EditAgentDefinitionDisplayContext {

	public EditAgentDefinitionDisplayContext(
		HttpServletRequest httpServletRequest, Portal portal) {

		_httpServletRequest = httpServletRequest;
		_portal = portal;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() {
		return "/o/ai-hub/v1.0/create-agent";
	}

	public Map<String, Object> getReactData() {
		String externalReferenceCode =
			_httpServletRequest.getParameter("externalReferenceCode");

		return HashMapBuilder.<String, Object>put(
			"backURL",
			() -> {
				Company company = _themeDisplay.getCompany();

				String backURL = company.getPortalURL(
					GroupConstants.DEFAULT_PARENT_GROUP_ID);

				return backURL + "/web/ai-hub/agents";
			}
		).put(
			"externalReferenceCode",
			() -> {
				if (externalReferenceCode == null) {
					return null;
				}

				return externalReferenceCode;
			}
		).put(
			"workflowDefinitionURL",
			() -> {
				Company company = _themeDisplay.getCompany();

				String namespace = _portal.getPortletNamespace(
					WorkflowPortletKeys.KALEO_DESIGNER);

				String url = StringBundler.concat(
					company.getPortalURL(
						GroupConstants.DEFAULT_PARENT_GROUP_ID),
					PropsValues.
						LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING,
					GroupConstants.CONTROL_PANEL_FRIENDLY_URL,
					PropsValues.CONTROL_PANEL_LAYOUT_FRIENDLY_URL);

				if (externalReferenceCode != null) {
					url = HttpComponentsUtil.addParameter(
						url, namespace + "name", externalReferenceCode);
				}

				return HttpComponentsUtil.addParameters(
					url, "p_p_id", WorkflowPortletKeys.KALEO_DESIGNER,
					"p_p_lifecycle", "0", "p_p_state",
					WindowState.MAXIMIZED.toString(), "p_p_mode",
					PortletMode.VIEW.toString(), namespace + "mvcPath",
					"/designer/edit_workflow_definition.jsp",
					namespace + "redirect",
					_portal.getPortalURL(_httpServletRequest) +
						_portal.getCurrentURL(_httpServletRequest),
					namespace + "clearSessionMessage", true,
					namespace + "scope", WorkflowDefinitionConstants.SCOPE_AI);
			}
		).build();
	}

	private final HttpServletRequest _httpServletRequest;
	private final Portal _portal;
	private final ThemeDisplay _themeDisplay;

}