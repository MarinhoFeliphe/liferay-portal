/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.site.initializer.internal.embedding.store;

import com.liferay.ai.hub.embedding.store.EmbeddingStoreFactory;
import com.liferay.ai.hub.embedding.store.EmbeddingStoreFactoryRegistry;
import com.liferay.osgi.service.tracker.collections.map.ServiceReferenceMapperFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;

import java.util.Objects;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Feliphe Marinho
 */
@Component(service = EmbeddingStoreFactoryRegistry.class)
public class EmbeddingStoreFactoryRegistryImpl
	implements EmbeddingStoreFactoryRegistry {

	@Override
	public EmbeddingStoreFactory getEmbeddingStoreFactory(String key) {
		if (Objects.equals(InMemoryEmbeddingStoreFactory.KEY, key)) {
			return new InMemoryEmbeddingStoreFactory();
		}

		return _serviceTrackerMap.getService(key);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, EmbeddingStoreFactory.class, null,
			ServiceReferenceMapperFactory.createFromFunction(
				bundleContext, EmbeddingStoreFactory::getKey));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private ServiceTrackerMap<String, EmbeddingStoreFactory> _serviceTrackerMap;

}