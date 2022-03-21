package com.savemywiki.tools.export.model;

/**
 * Application Model adapter.
 * 
 * @author Marc Alemany
 */
public class ModelAdapter implements IModelListener {

	@Override
	public void onApplicationStateChange(AppState state) {
	}

	@Override
	public void onExportInitDone(ExportData data) {
	}

	@Override
	public void onExportDone(ExportData data) {
	}

	@Override
	public void onNamespaceDataUpdate(WikiNamespaceData wikiData, TaskStatus oldStatus, TaskStatus newStatus) {
	}

}
