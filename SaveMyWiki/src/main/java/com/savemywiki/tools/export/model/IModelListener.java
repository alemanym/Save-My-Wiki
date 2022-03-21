package com.savemywiki.tools.export.model;

public interface IModelListener {
	
	void onApplicationStateChange(AppState state);
	
	void onExportInitDone(ExportData data);
	
	void onExportDone(ExportData data);
	
	void onNamespaceDataUpdate(WikiNamespaceData wikiData, TaskStatus oldStatus, TaskStatus newStatus);

}
