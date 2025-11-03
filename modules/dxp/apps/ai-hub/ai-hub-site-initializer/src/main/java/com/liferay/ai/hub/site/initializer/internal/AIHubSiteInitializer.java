/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.workflow.constants.WorkflowDefinitionConstants;
import com.liferay.portal.workflow.manager.WorkflowDefinitionManager;
import com.liferay.site.exception.InitializationException;
import com.liferay.site.initializer.SiteInitializer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(
	property = "site.initializer.key=" + AIHubSiteInitializer.KEY,
	service = SiteInitializer.class
)
public class AIHubSiteInitializer implements SiteInitializer {

	public static final String KEY = "ai-hub-initializer";

	@Override
	public String getDescription(Locale locale) {
		return StringPool.BLANK;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName(Locale locale) {
		return _language.get(locale, "ai-hub");
	}

	@Override
	public String getThumbnailSrc() {
		return StringPool.BLANK;
	}

	@Override
	public void initialize(long groupId) throws InitializationException {
		try {
			_initialize(groupId);
		}
		catch (InitializationException initializationException) {
			throw initializationException;
		}
		catch (Exception exception) {
			throw new InitializationException(exception);
		}
	}

	@Override
	public boolean isActive(long companyId) {
		return FeatureFlagManagerUtil.isEnabled(companyId, "LPD-62272");
	}

	private void _deployWorkflowDefinition(
			Company company, String externalReferenceCode, String json,
			String workflowDefinitionName)
		throws Exception {

		Map<String, String> titleMap = new HashMap<>();

		for (Locale locale :
				_language.getCompanyAvailableLocales(company.getCompanyId())) {

			titleMap.put(
				_language.getLanguageId(locale),
				_language.get(locale, workflowDefinitionName));
		}

		_workflowDefinitionManager.deployWorkflowDefinition(
			externalReferenceCode, company.getCompanyId(),
			PrincipalThreadLocal.getUserId(),
			_localization.getXml(
				titleMap, _language.getLanguageId(company.getLocale()),
				"title"),
			workflowDefinitionName, json.getBytes());
	}

	private void _initialize(long groupId) throws Exception {
		Group group = _groupLocalService.getGroup(groupId);

		int count = _workflowDefinitionManager.getWorkflowDefinitionsCount(
			group.getCompanyId(),
			WorkflowDefinitionConstants.NAME_IMPROVE_WRITING);

		if (count > 0) {
			return;
		}

		Company company = _companyLocalService.getCompany(group.getCompanyId());

		String json1 = StringUtil.read(
			AIHubSiteInitializer.class.getResourceAsStream(
				"dependencies/improve-writing-workflow-definition.json"));

		_deployWorkflowDefinition(
			company,
			WorkflowDefinitionConstants.EXTERNAL_REFERENCE_CODE_IMPROVE_WRITING,
			json1, WorkflowDefinitionConstants.NAME_IMPROVE_WRITING);

		String json2 = StringUtil.read(
			AIHubSiteInitializer.class.getResourceAsStream(
				"dependencies/fix-spelling-and-grammar-workflow-definition." +
					"json"));

		_deployWorkflowDefinition(
			company,
			WorkflowDefinitionConstants.
				EXTERNAL_REFERENCE_CODE_FIX_SPELLING_AND_GRAMMAR,
			json2, WorkflowDefinitionConstants.NAME_FIX_SPELLING_AND_GRAMMAR);
	}

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Language _language;

	@Reference
	private Localization _localization;

	@Reference
	private WorkflowDefinitionManager _workflowDefinitionManager;

}