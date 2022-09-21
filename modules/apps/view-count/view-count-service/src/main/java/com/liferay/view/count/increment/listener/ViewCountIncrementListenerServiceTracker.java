package com.liferay.view.count.increment.listener;


import com.liferay.osgi.service.tracker.collections.map.ServiceReferenceMapperFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(
	immediate = true,
	service = ViewCountIncrementListenerServiceTracker.class
)
public class ViewCountIncrementListenerServiceTracker {

	public ViewCountIncrementListener getViewCountIncrementListener(String modelClassName) {

		return _serviceTrackerMap.getService(modelClassName);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, ViewCountIncrementListener.class, null,
			ServiceReferenceMapperFactory.createFromFunction(
				bundleContext, ViewCountIncrementListener::getModelClassName));
	}

	@Deactivate
	protected void deactivate()
	{
		_serviceTrackerMap.close();
	}

	private ServiceTrackerMap<String, ViewCountIncrementListener>
		_serviceTrackerMap;
}
