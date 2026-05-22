/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.guardrail;

import java.util.Map;

/**
 * @author Feliphe Marinho
 * @author João Victor Alves
 */
public interface ModelArmorTemplateHandler {

	public void createModelArmorTemplate(
			boolean active, long companyId, String description,
			String externalReferenceCode, String guardrailType, String location,
			boolean maliciousUriFilterEnabled,
			boolean multiLanguageDetectionEnabled,
			String piAndJailbreakConfidenceLevel,
			boolean piAndJailbreakFilterEnabled, String raiDangerousLevel,
			String raiHarassmentLevel, String raiHateSpeechLevel,
			String raiSexuallyExplicitLevel, boolean sdpFilterEnabled,
			Map<String, String> titleMap)
		throws Exception;

	public void deleteModelArmorTemplate(
			long companyId, String externalReferenceCode, String location)
		throws Exception;

	public void updateModelArmorTemplate(
			boolean active, long companyId, String description,
			String externalReferenceCode, String guardrailType, String location,
			boolean maliciousUriFilterEnabled,
			boolean multiLanguageDetectionEnabled,
			String piAndJailbreakConfidenceLevel,
			boolean piAndJailbreakFilterEnabled, String raiDangerousLevel,
			String raiHarassmentLevel, String raiHateSpeechLevel,
			String raiSexuallyExplicitLevel, boolean sdpFilterEnabled,
			Map<String, String> titleMap)
		throws Exception;

}
