package com.savemywiki.tools.export.ui.table;

import java.util.List;

public interface PaginationDataProvider<T> {
	int getTotalRowCount();

	List<T> getRows(int startIndex, int endIndex);
}