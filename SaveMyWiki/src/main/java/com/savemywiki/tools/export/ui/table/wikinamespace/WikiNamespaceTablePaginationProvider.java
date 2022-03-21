package com.savemywiki.tools.export.ui.table.wikinamespace;

import java.util.List;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.WikiNamespaceData;
import com.savemywiki.tools.export.ui.table.PaginationDataProvider;

public class WikiNamespaceTablePaginationProvider implements PaginationDataProvider<WikiNamespaceData> {
	
	private AppModel model;

	public WikiNamespaceTablePaginationProvider(AppModel model) {
		this.model = model;
	}

	public int getTotalRowCount() {
		return this.model.getWikiNamespaceDataList().size();
	}

	public List<WikiNamespaceData> getRows(int startIndex, int endIndex) {
		return this.model.getWikiNamespaceDataList().subList(startIndex, endIndex);
	}
}
