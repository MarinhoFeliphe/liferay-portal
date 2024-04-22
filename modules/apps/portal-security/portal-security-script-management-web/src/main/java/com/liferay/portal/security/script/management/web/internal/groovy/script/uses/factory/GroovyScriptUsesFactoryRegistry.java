/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.script.management.web.internal.groovy.script.uses.factory;

import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.portal.kernel.db.partition.DBPartition;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.script.management.groovy.script.use.GroovyScriptUse;
import com.liferay.portal.security.script.management.groovy.script.uses.factory.GroovyScriptUsesFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(service = GroovyScriptUsesFactoryRegistry.class)
public class GroovyScriptUsesFactoryRegistry {

	public List<GroovyScriptUse> getGroovyScriptUses(Locale locale)
		throws Exception {

		List<GroovyScriptUse> groovyScriptUses = new ArrayList<>();

		if (DBPartition.isPartitionEnabled()) {
			_companyLocalService.forEachCompanyId(
				companyId -> _addGroovyScriptUses(groovyScriptUses, locale));
		}
		else {
			_addGroovyScriptUses(groovyScriptUses, locale);
		}

		Comparator<GroovyScriptUse> comparator = Comparator.comparing(
			GroovyScriptUse::getCompanyWebId);

		groovyScriptUses.sort(
			comparator.thenComparing(
				groovyScriptUse -> StringUtil.lowerCase(
					groovyScriptUse.getSourceName())));

		return groovyScriptUses;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, GroovyScriptUsesFactory.class);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerList.close();
	}

	private void _addGroovyScriptUses(
			List<GroovyScriptUse> groovyScriptUses, Locale locale)
		throws Exception {

		Iterator<GroovyScriptUsesFactory> iterator =
			_serviceTrackerList.iterator();

		while (iterator.hasNext()) {
			GroovyScriptUsesFactory groovyScriptUsesFactory = iterator.next();

			groovyScriptUses.addAll(groovyScriptUsesFactory.create(locale));
		}
	}

	@Reference
	private CompanyLocalService _companyLocalService;

	private ServiceTrackerList<GroovyScriptUsesFactory> _serviceTrackerList;

}