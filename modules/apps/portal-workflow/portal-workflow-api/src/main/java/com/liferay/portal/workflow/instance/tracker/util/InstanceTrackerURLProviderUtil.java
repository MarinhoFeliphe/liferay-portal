/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.instance.tracker.util;

import com.liferay.osgi.util.ServiceTrackerFactory;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Feliphe Marinho
 */
public class InstanceTrackerURLProviderUtil {

	public static String getURL(
		Object bean, Class<?> model, HttpServletRequest httpServletRequest)
		throws Exception {

		InstanceTrackerURLProvider instanceTrackerURLProvider = _serviceTracker.getService();

		return instanceTrackerURLProvider.getURL(bean, model, httpServletRequest);
	}

	private static final ServiceTracker<InstanceTrackerURLProvider, InstanceTrackerURLProvider>
		_serviceTracker = ServiceTrackerFactory.open(
		FrameworkUtil.getBundle(InstanceTrackerURLProviderUtil.class),
		InstanceTrackerURLProvider.class);
}
