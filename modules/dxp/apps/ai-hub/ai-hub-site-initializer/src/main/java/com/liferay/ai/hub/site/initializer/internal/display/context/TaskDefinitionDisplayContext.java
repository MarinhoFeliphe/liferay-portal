/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.display.context;

import com.liferay.ai.hub.site.initializer.internal.display.context.helper.TaskDefinitionRequestHelper;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.PortalUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

/**
 * @author João Victor Alves
 */
public class TaskDefinitionDisplayContext {

	public TaskDefinitionDisplayContext(
		HttpServletRequest httpServletRequest,
		LiferayPortletResponse liferayPortletResponse) {

		_httpServletRequest = httpServletRequest;

		TaskDefinitionRequestHelper taskDefinitionRequestHelper =
			new TaskDefinitionRequestHelper(httpServletRequest);

		_liferayPortletResponse = liferayPortletResponse;
	}

	private HttpServletRequest _httpServletRequest;
	private LiferayPortletResponse _liferayPortletResponse;

	public String getAPIURL() {
		return "/o/ai-hub/v1.0/task-definitions";
	}

	public CreationMenu getCreationMenu() throws Exception {
		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(_URL);
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "new-workflow"));
			}
		).build();
	}

	private static final String _URL =
		"http://localhost:8080/group/control_panel/manage?" +
		"p_p_id=com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet&" +
		"p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&" +
		"_com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet_mvcPath=/designer/edit_workflow_definition.jsp&" +
		"p_p_lifecycle=0&p_p_state=maximized&" +
		"_com_liferay_portal_workflow_web_portlet_ControlPanelWorkflowPortlet_mvcPath=%2Fview.jsp&" +
		"_com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet_clearSessionMessage=true";

	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		return Arrays.asList(
			new FDSActionDropdownItem(
				_URL_2, "view", "view",
				"view",
				"get", null, null));
	}


	private static final String _URL_2 =
		"http://localhost:8080/group/control_panel/manage?p_p_id=com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet&" +
		"p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&" +
		"_com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet_redirect=http%3A%2F%2Flocalhost%3A8080%2Fgroup%2Fcontrol_panel%2Fmanage%3Fp_p_id%3Dcom_liferay_portal_workflow_web_portlet_ControlPanelWorkflowPortlet%26p_p_lifecycle%3D0%26p_p_state%3Dmaximized%26p_p_mode%3Dview%26_com_liferay_portal_workflow_web_portlet_ControlPanelWorkflowPortlet_mvcPath%3D%252Fview.jsp&" +
		"_com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet_mvcPath=%2Fdesigner%2Fedit_workflow_definition.jsp&" +
		"_com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet_jsp_state=view&" +
		"_com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet_name=Chat Message Pipeline&" +
		"_com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet_draftVersion=1.0";
}