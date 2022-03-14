package com.savemywiki.tools.export.model;

public interface IModelListener {
	
	void onApplicationStateChange(AppState state);
	
	void onExportDone(ExportData data);

}
