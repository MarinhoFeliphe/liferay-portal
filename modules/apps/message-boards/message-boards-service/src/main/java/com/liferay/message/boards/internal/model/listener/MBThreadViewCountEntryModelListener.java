package com.liferay.message.boards.internal.model.listener;

import com.liferay.message.boards.model.MBThread;
import com.liferay.message.boards.service.MBThreadLocalService;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.view.count.entry.model.listener.ViewCountEntryModelListener;
import com.liferay.view.count.model.ViewCountEntry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ViewCountEntryModelListener.class)
public class MBThreadViewCountEntryModelListener implements
	ViewCountEntryModelListener {

	@Override
	public void afterIncrementListener(
		ViewCountEntry viewCountEntry) {

		MBThread mbThread = _mbThreadLocalService.fetchMBThread(viewCountEntry.getClassPK());

		Indexer<MBThread> indexer = IndexerRegistryUtil.nullSafeGetIndexer(
			MBThread.class);

		try {
			indexer.reindex(mbThread);
		}
		catch (SearchException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public String getModelClassName() {
		return MBThread.class.getName();
	}

	@Reference
	private MBThreadLocalService _mbThreadLocalService;
}
