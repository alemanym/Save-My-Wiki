package com.savemywiki.tools.export.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppModel {

	// Config
	private String frameTitle;
	private String websiteURL;
	private int namesQueryLimit;
	private long queryPauseTime;
	private String appIconURL;

	// Export data
	private List<WikiNamespaceData> wikiNamespaceDataList;
	private List<ExportData> exportDataList;
	private List<ExportData> retryList;
	private File exportsDataFile;

	// Process
	private AppState applicationState;
	private TaskStatus currenProcessState;
	private String nextPage;
	private Date startDate;
	private Date endDate;

	private List<IModelListener> listeners;

	// Constructor

	public AppModel() {
		this.exportDataList = new ArrayList<>();
		this.retryList = new ArrayList<>();
		this.listeners = new ArrayList<>();
		this.wikiNamespaceDataList = new ArrayList<>();
		for (WikiNamespace namespace : WikiNamespace.values()) {
			wikiNamespaceDataList.add(new WikiNamespaceData(namespace));
		}
	}

	public void clearData() {
		exportDataList.clear();
		for (WikiNamespaceData data : wikiNamespaceDataList) {
			data.clear();
		}
	}

	// Getters & Setters

	public String getFrameTitle() {
		return frameTitle;
	}

	public void setFrameTitle(String frameTitle) {
		this.frameTitle = frameTitle;
	}

	public String getWebsiteURL() {
		return websiteURL;
	}

	public void setWebsiteURL(String websiteURL) {
		this.websiteURL = websiteURL;
	}

	public int getNamesQueryLimit() {
		return namesQueryLimit;
	}

	public void setNamesQueryLimit(int namesQueryLimit) {
		this.namesQueryLimit = namesQueryLimit;
	}

	public long getQueryPauseTime() {
		return queryPauseTime;
	}

	public void setQueryPauseTime(long queryPauseTime) {
		this.queryPauseTime = queryPauseTime;
	}

	public String getAppIconURL() {
		return appIconURL;
	}

	public void setAppIconURL(String appIconURL) {
		this.appIconURL = appIconURL;
	}

	public List<WikiNamespaceData> getWikiNamespaceDataList() {
		return wikiNamespaceDataList;
	}

	public void setWikiNamespaceDataList(List<WikiNamespaceData> wikiNamespaceDataList) {
		this.wikiNamespaceDataList = wikiNamespaceDataList;
	}

	public List<ExportData> getExportDataList() {
		return exportDataList;
	}

	public List<ExportData> getRetryList() {
		return retryList;
	}

	public int countNames(WikiNamespace namespace) {
		int count = 0;
		for (ExportData data : exportDataList) {
			if (data.getNamespace() == namespace) {
				count += data.getPageNames().size();
			}
		}
		return count;
	}

	public int countNames() {
		int count = 0;
		for (ExportData data : exportDataList) {
			count += data.getPageNames().size();
		}
		return count;
	}

	public String getNextPage() {
		return nextPage;
	}

	public void setNextPage(String nextPage) {
		this.nextPage = nextPage;
	}

	public AppState getApplicationState() {
		return applicationState;
	}

	public void setApplicationState(AppState state) {
		this.applicationState = state;
		for (IModelListener listener : listeners) {
			listener.onApplicationStateChange(this.applicationState);
		}
	}

	public TaskStatus getCurrenProcessState() {
		return currenProcessState;
	}

	public void setCurrenProcessState(TaskStatus currenProcessState) {
		this.currenProcessState = currenProcessState;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public synchronized void addActionListener(IModelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void addRetry(ExportData exportData) {
		ExportData data1 = new ExportData(exportData.getNamespace());
		data1.setRetrySource(exportData);
		data1.setId(1);
		ExportData data2 = new ExportData(exportData.getNamespace());
		data2.setRetrySource(exportData);
		data2.setId(2);
		int newSize = exportData.getPageNames().size() / 2;
		int n = 0;
		for (String name : exportData.getPageNames()) {
			if (n < newSize) {
				data1.getPageNames().add(name);
			} else {
				data2.getPageNames().add(name);
			}
			n++;
		}
		retryList.add(data1);
		retryList.add(data2);
	}

	public int countExportSuccess() {
		int count = 0;
		for (ExportData exportData : exportDataList) {
			if (exportData.getStatus() == TaskStatus.DONE_SUCCESS) {
				count += exportData.getPageNames().size();
			}
		}
		for (ExportData exportData : retryList) {
			if (exportData.getStatus() == TaskStatus.DONE_SUCCESS) {
				count += exportData.getPageNames().size();
			}
		}
		return count;
	}

	public void setExportStatus(ExportData data, TaskStatus status) {
		data.setStatus(status);
		for (IModelListener listener : listeners) {
			listener.onExportDone(data);
		}
	}

	public void setExportsDataFile(File exportsDataFile) {
		this.exportsDataFile = exportsDataFile;
	}

	public File getExportsDataFile() {
		return exportsDataFile;
	}

	public String fromStartDate() {
		long millisec = new Date().getTime() - getStartDate().getTime();
		long sec = (millisec / 1000) % 60;
		long min = millisec / 60000;
		return (min > 9 ? min : "0" + min) + ":" + (sec > 9 ? sec : "0" + sec);
	}

	public ExportData getLastExportData() {
		return getExportDataList().get(getExportDataList().size() - 1);
	}

	public ExportData getLastExportData(WikiNamespace namespace) {
		ExportData res = null;
		for (ExportData exportData : exportDataList) {
			if (exportData.getNamespace() == namespace) {
				res = exportData;
			}
		}
		return res;
	}

	public ExportData getFirstExportData(WikiNamespace namespace) {
		ExportData res = null;
		for (ExportData exportData : exportDataList) {
			if (exportData.getNamespace() == namespace) {
				res = exportData;
				break;
			}
		}
		return res;
	}

	public void add(ExportData exportData) {
		getExportDataList().add(exportData);
		WikiNamespaceData wikiData = getWikiNamespaceData(exportData.getNamespace());
		wikiData.add(exportData);
		for (IModelListener listener : listeners) {
			listener.onExportInitDone(exportData);
			listener.onNamespaceDataUpdate(wikiData, wikiData.getReadStatus(), wikiData.getReadStatus());
		}
	}

	public void setWikiNamespaceReadStatus(WikiNamespace namespace, TaskStatus status) {
		WikiNamespaceData wikiData = getWikiNamespaceData(namespace);
		TaskStatus oldStatus = wikiData.getReadStatus();
		wikiData.setReadStatus(status);
		for (IModelListener listener : listeners) {
			listener.onNamespaceDataUpdate(wikiData, oldStatus, status);
		}
	}

	public void setWikiNamespaceExportStatus(WikiNamespace namespace, TaskStatus status) {
		WikiNamespaceData wikiData = getWikiNamespaceData(namespace);
		TaskStatus oldStatus = wikiData.getExportStatus();
		wikiData.setExportStatus(status);
		for (IModelListener listener : listeners) {
			listener.onNamespaceDataUpdate(wikiData, oldStatus, status);
		}
	}

	public WikiNamespaceData getWikiNamespaceData(WikiNamespace namespace) {
		for (WikiNamespaceData data : wikiNamespaceDataList) {
			if (data.getNamespace() == namespace) {
				return data;
			}
		}
		return null;
	}

}
