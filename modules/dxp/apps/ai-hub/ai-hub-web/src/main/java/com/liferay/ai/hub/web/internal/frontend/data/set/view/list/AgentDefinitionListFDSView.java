/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.web.internal.frontend.data.set.view.list;

import com.liferay.ai.hub.web.internal.constants.AIHubFDSNames;
import com.liferay.frontend.data.set.view.FDSView;
import com.liferay.frontend.data.set.view.list.BaseListFDSView;
import com.liferay.frontend.data.set.view.list.FDSListSchema;
import com.liferay.frontend.data.set.view.list.FDSListSchemaBuilder;
import com.liferay.frontend.data.set.view.list.FDSListSchemaBuilderFactory;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;

/**
 * @author João Victor Alves
 */
@Component(
	property = "frontend.data.set.name=" + AIHubFDSNames.AGENT_DEFINITIONS,
	service = FDSView.class
)
public class AgentDefinitionListFDSView extends BaseListFDSView {

	@Override
	public String getDescription() {
		return "description";
	}

	@Override
	public FDSListSchema getFDSListSchema(Locale locale) {
		FDSListSchemaBuilderFactory fdsListSchemaBuilderFactory =
			_fdsListSchemaBuilderFactorySnapshot.get();

		if (fdsListSchemaBuilderFactory == null) {
			return null;
		}

		FDSListSchemaBuilder fdsListSchemaBuilder =
			fdsListSchemaBuilderFactory.create();

		return fdsListSchemaBuilder.add(
			"labels.label.displayTypeKey",
			HashMapBuilder.put(
				"active", "success"
			).put(
				"custom", "warning"
			).put(
				"inactive", "danger"
			).put(
				"system", "info"
			).build(),
			"labels.label.value"
		).build();
	}

	@Override
	public String getTitle() {
		return "title";
	}

	private static final Snapshot<FDSListSchemaBuilderFactory>
		_fdsListSchemaBuilderFactorySnapshot = new Snapshot<>(
			AgentDefinitionListFDSView.class,
			FDSListSchemaBuilderFactory.class);

}