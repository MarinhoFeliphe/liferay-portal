package com.liferay.view.count.increment.listener;

import com.liferay.view.count.model.ViewCountEntry;

public interface ViewCountIncrementListener {

	public void afterIncrementListener(ViewCountEntry viewCountEntry);

	public String getModelClassName();
}
