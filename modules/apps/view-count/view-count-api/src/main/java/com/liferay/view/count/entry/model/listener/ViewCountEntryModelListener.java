package com.liferay.view.count.entry.model.listener;

import com.liferay.view.count.model.ViewCountEntry;

public interface ViewCountEntryModelListener {

	public void afterIncrementListener(ViewCountEntry viewCountEntry);

	public String getModelClassName();
}
