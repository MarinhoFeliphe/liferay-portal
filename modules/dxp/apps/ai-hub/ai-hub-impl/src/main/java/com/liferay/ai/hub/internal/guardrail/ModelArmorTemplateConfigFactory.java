/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.guardrail;

import com.google.cloud.modelarmor.v1.DetectionConfidenceLevel;
import com.google.cloud.modelarmor.v1.RaiFilterType;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.CamelCaseUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author João Victor Alves
 */
public class ModelArmorTemplateConfigFactory {

	public static ModelArmorTemplateConfig get(
		String templateId, Map<String, ?> values) {

		Map<RaiFilterType, DetectionConfidenceLevel> raiFilters = new EnumMap<>(
			RaiFilterType.class);

		_putRaiFilter(
			raiFilters, RaiFilterType.DANGEROUS,
			values.get("raiDangerousLevel"));
		_putRaiFilter(
			raiFilters, RaiFilterType.HARASSMENT,
			values.get("raiHarassmentLevel"));
		_putRaiFilter(
			raiFilters, RaiFilterType.HATE_SPEECH,
			values.get("raiHateSpeechLevel"));
		_putRaiFilter(
			raiFilters, RaiFilterType.SEXUALLY_EXPLICIT,
			values.get("raiSexuallyExplicitLevel"));

		return ModelArmorTemplateConfig.builder(
			templateId
		).guardrailType(
			GetterUtil.getString(values.get("guardrailType"), "input")
		).location(
			GetterUtil.getString(values.get("location"))
		).maliciousUriFilterEnabled(
			GetterUtil.getBoolean(values.get("maliciousUriFilterEnabled"))
		).multiLanguageDetectionEnabled(
			GetterUtil.getBoolean(values.get("multiLanguageDetectionEnabled"))
		).name(
			GetterUtil.getString(values.get("name"))
		).piAndJailbreakFilterEnabled(
			GetterUtil.getBoolean(values.get("piAndJailbreakFilterEnabled"))
		).piAndJailbreakConfidenceLevel(
			_toConfidenceLevel(
				GetterUtil.getString(
					values.get("piAndJailbreakConfidenceLevel"),
					"mediumAndAbove"))
		).raiFilters(
			raiFilters
		).sdpFilterEnabled(
			GetterUtil.getBoolean(values.get("sdpFilterEnabled"))
		).build();
	}

	private static void _putRaiFilter(
		Map<RaiFilterType, DetectionConfidenceLevel> raiFilters,
		RaiFilterType raiFilterType, Object levelProperty) {

		if (Objects.equals(
				GetterUtil.getString(levelProperty, "none"), "none")) {

			return;
		}

		raiFilters.put(
			raiFilterType,
			_toConfidenceLevel(GetterUtil.getString(levelProperty, "none")));
	}

	private static DetectionConfidenceLevel _toConfidenceLevel(String key) {
		try {
			return DetectionConfidenceLevel.valueOf(_toEnumKey(key));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unknown confidence level " + key +
						", falling back to MEDIUM_AND_ABOVE",
					illegalArgumentException);
			}

			return DetectionConfidenceLevel.MEDIUM_AND_ABOVE;
		}
	}

	private static String _toEnumKey(String key) {
		if (Validator.isNull(key)) {
			return StringPool.BLANK;
		}

		return StringUtil.toUpperCase(
			CamelCaseUtil.fromCamelCase(key, CharPool.UNDERLINE));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ModelArmorTemplateConfigFactory.class);

}
