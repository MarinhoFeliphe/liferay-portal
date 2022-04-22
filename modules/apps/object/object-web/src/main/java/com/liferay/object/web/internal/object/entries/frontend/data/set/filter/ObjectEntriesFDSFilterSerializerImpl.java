package com.liferay.object.web.internal.object.entries.frontend.data.set.filter;

import com.liferay.frontend.data.set.filter.FDSFilter;
import com.liferay.frontend.data.set.filter.FDSFilterContextContributor;
import com.liferay.frontend.data.set.filter.FDSFilterContextContributorRegistry;
import com.liferay.frontend.data.set.filter.FDSFilterRegistry;
import com.liferay.frontend.data.set.filter.FDSFilterSerializer;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@Component(
	immediate = true,
	property = "frontend.data.set.filter.serializer.key=object_entries",
	service = FDSFilterSerializer.class)
public class ObjectEntriesFDSFilterSerializerImpl implements
	FDSFilterSerializer {

	@Override
	public JSONArray serialize(String fdsName, Locale locale) {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());

		List<FDSFilter> fdsFilters = _fdsFilterRegistry.getFDSFilters(fdsName);

		for (FDSFilter fdsFilter : fdsFilters) {
			String label = LanguageUtil.get(
				resourceBundle, fdsFilter.getLabel());

			JSONObject jsonObject = JSONUtil.put(
				"id", fdsFilter.getId()
			).put(
				"label", label
			).put(
				"preloadedData", fdsFilter.getPreloadedData()
			).put(
				"type", fdsFilter.getType()
			);

			List<FDSFilterContextContributor> fdsFilterContextContributors =
				_fdsFilterContextContributorRegistry.
					getFDSFilterContextContributors(fdsFilter.getType());

			for (FDSFilterContextContributor fdsFilterContextContributor :
				fdsFilterContextContributors) {

				Map<String, Object> fdsFilterContext =
					fdsFilterContextContributor.getFDSFilterContext(
						fdsFilter, locale);

				if (fdsFilterContext == null) {
					continue;
				}

				for (Map.Entry<String, Object> entry :
					fdsFilterContext.entrySet()) {

					jsonObject.put(entry.getKey(), entry.getValue());
				}
			}

			jsonArray.put(jsonObject);
		}

		return jsonArray;
	}

	@Reference
	private FDSFilterContextContributorRegistry
		_fdsFilterContextContributorRegistry;

	@Reference
	private FDSFilterRegistry _fdsFilterRegistry;

	@Reference
	private JSONFactory _jsonFactory;

}
