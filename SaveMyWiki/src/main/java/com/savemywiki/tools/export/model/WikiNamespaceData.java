package com.savemywiki.tools.export.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data about wiki by namespace.
 * 
 * @author Marc Alemany
 */
public class WikiNamespaceData {

	private WikiNamespace namespace;
	private List<ExportData> exportDataList;
	private String message;
	private TaskStatus readStatus;
	private TaskStatus exportStatus;
	
	// constructor

	public WikiNamespaceData(WikiNamespace namespace) {
		this.namespace = namespace;
		this.exportDataList = new ArrayList<ExportData>();
		readStatus = TaskStatus.UNDEFINED;
		exportStatus = TaskStatus.UNDEFINED;
	}

	public void clear() {
		exportDataList.clear();
		readStatus = TaskStatus.UNDEFINED;
		exportStatus = TaskStatus.UNDEFINED;
	}

	public void add(ExportData exportData) {
		this.getExportDataList().add(exportData);
	}
	
	// getters & setters

	public WikiNamespace getNamespace() {
		return namespace;
	}

	public void setNamespace(WikiNamespace namespace) {
		this.namespace = namespace;
	}

	public List<ExportData> getExportDataList() {
		return exportDataList;
	}

	public void setExportDataList(List<ExportData> exportDataList) {
		this.exportDataList = exportDataList;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public TaskStatus getReadStatus() {
		return readStatus;
	}

	public void setReadStatus(TaskStatus readStatus) {
		this.readStatus = readStatus;
	}

	public TaskStatus getExportStatus() {
		return exportStatus;
	}

	public void setExportStatus(TaskStatus exportStatus) {
		this.exportStatus = exportStatus;
	}

}
