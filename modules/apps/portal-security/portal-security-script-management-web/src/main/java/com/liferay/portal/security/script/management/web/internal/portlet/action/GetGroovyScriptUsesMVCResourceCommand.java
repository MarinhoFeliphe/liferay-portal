/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.script.management.web.internal.portlet.action;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.security.script.management.groovy.script.use.GroovyScriptUse;
import com.liferay.portal.security.script.management.web.internal.groovy.script.uses.factory.GroovyScriptUsesFactoryRegistry;

import java.util.List;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(
	property = {
		"javax.portlet.name=" + ConfigurationAdminPortletKeys.SYSTEM_SETTINGS,
		"mvc.command.name=/system_settings/get_groovy_script_uses"
	},
	service = MVCResourceCommand.class
)
public class GetGroovyScriptUsesMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		List<GroovyScriptUse> groovyScriptUses =
			_groovyScriptUsesFactoryRegistry.getGroovyScriptUses(
				resourceResponse.getLocale());

		for (GroovyScriptUse groovyScriptUse : groovyScriptUses) {
			jsonArray.put(
				JSONUtil.put(
					"companyWebId", groovyScriptUse.getCompanyWebId()
				).put(
					"sourceName", groovyScriptUse.getSourceName()
				).put(
					"sourceURL", groovyScriptUse.getSourceURL()
				));
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, jsonArray);
	}

	@Reference
	private GroovyScriptUsesFactoryRegistry _groovyScriptUsesFactoryRegistry;

	@Reference
	private JSONFactory _jsonFactory;

}