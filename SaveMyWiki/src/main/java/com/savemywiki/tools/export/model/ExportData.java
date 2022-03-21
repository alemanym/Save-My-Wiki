package com.savemywiki.tools.export.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data about an export package.
 * 
 *  @author Marc Alemany
 */
public class ExportData {

	private int id;
	private WikiNamespace namespace;
	private List<String> pageNames;
	private String exportXML;
	private ExportData retrySource;
	private TaskStatus status;

	public ExportData(WikiNamespace namespace) {
		this.namespace = namespace;
		this.pageNames = new ArrayList<String>(500);
		this.status = TaskStatus.UNDEFINED;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public WikiNamespace getNamespace() {
		return namespace;
	}

	public void setNamespace(WikiNamespace namespace) {
		this.namespace = namespace;
	}

	public List<String> getPageNames() {
		return pageNames;
	}

	public String getExportXML() {
		return exportXML;
	}

	public void setExportXML(String exportXML) {
		this.exportXML = exportXML;
	}
	
	public TaskStatus getStatus() {
		return status;
	}

	protected void setStatus(TaskStatus status) {
		this.status = status;
	}

	public boolean isRetry() {
		return retrySource != null;
	}

	public void setRetrySource(ExportData retry) {
		this.retrySource = retry;
	}

	public ExportData getRetrySource() {
		return this.retrySource;
	}
	
}
