package com.liferay.view.count.increment.listener;

import com.liferay.view.count.model.ViewCountEntry;

public interface ViewCountEntryModelListener {

	public void afterIncrement(ViewCountEntry viewCountEntry);

	public String getModelClassName();
}
