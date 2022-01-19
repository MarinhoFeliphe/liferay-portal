package com.liferay.object.web.internal.object.entries.display.context;

import com.liferay.osgi.util.ServiceTrackerFactory;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.http.HttpServletRequest;

public class ViewObjectEntryDisplayContextProviderUtil {

	public static ViewObjectEntryDisplayContext getViewObjectEntryDisplayContext(
		HttpServletRequest httpServletRequest) {

		ViewObjectEntryDisplayContextProvider viewObjectEntryDisplayContextProvider =
			_serviceTracker.getService();

		return viewObjectEntryDisplayContextProvider.getViewObjectEntryDisplayContext(
			httpServletRequest);
	}

	private static final ServiceTracker
		<ViewObjectEntryDisplayContextProvider, ViewObjectEntryDisplayContextProvider>
		_serviceTracker = ServiceTrackerFactory.open(
		FrameworkUtil.getBundle(ViewObjectEntryDisplayContextProviderUtil.class),
		ViewObjectEntryDisplayContextProvider.class);
}
