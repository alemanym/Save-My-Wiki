package com.savemywiki.tools.export.ctrl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.savemywiki.mediawiki.service.MediawikiService;
import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.AppState;
import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.TaskStatus;
import com.savemywiki.tools.export.model.WikiNamespace;
import com.savemywiki.tools.export.ui.AppView;
import com.savemywiki.tools.export.ui.IActionListener;
import com.savemywiki.tools.export.ui.UIHelper;
import com.savemywiki.utils.AbortProcessException;
import com.savemywiki.utils.FileHelper;
import com.savemywiki.utils.FileHelper.ZipFileFilter;
import com.savemywiki.utils.Logger;

public class AppController implements IActionListener {

	private static final int MAX_PAGES_BY_QUERY_LIST = 200;
	private static final String SKIP_LINE = "\r\n";

	private AppModel model;
	private AppView view;
	private MediawikiService wikiService;
	private FileHelper fileHelper;
	private ProcessRunManager processRunManager;
	private Logger logger;

	public void init() {

		// init model
		model = new AppModel();
		model.setAppIconURL("/assets/app-icon.png");
		model.setFrameTitle("Save My Wiki");
		model.setWebsiteURL("https://omnis-bibliotheca.com");
		model.setNamesQueryLimit(MAX_PAGES_BY_QUERY_LIST);

		// create view
		UIHelper uiHelper = new UIHelper();
		view = new AppView(uiHelper, model);
		view.addActionListener(this);
		logger = new Logger(model, view);

		fileHelper = new FileHelper();
		processRunManager = new ProcessRunManager(model, logger);
		wikiService = new MediawikiService(model, processRunManager, logger);
	}

	/**
	 * Retrieve all wiki page names.
	 */
	@Override
	public void performGetPageNames() {
		model.setStartDate(new Date());
		model.setCurrenProcessState(TaskStatus.PROCESSING);
		model.setApplicationState(AppState.GET_NAMES_PROCESSING);
		model.clearData();
		logger.appendProgress(
				"<span style=\"color: orange;\">-----------------------------------<br>Récupération des noms de page<br>-----------------------------------</span>");
		try {
			for (WikiNamespace namespace : WikiNamespace.values()) {
				model.setWikiNamespaceReadStatus(namespace, TaskStatus.PROCESSING);

				// for each wiki namespace

				logger.appendProgress("\r\n[" + model.fromStartDate() + "] Type de page : <b style=\"color: white;\">"
						+ namespace.desc() + "</b>");
				logger.appendLog("Espace de nom : " + namespace.name() + " (id : " + namespace.getId() + ")");

				// first call wiki web service
				model.setNextPage("");
				int iteration = 1;
				logger.appendLog("Récupération des noms de page...");
				ExportData data = wikiService.retrievePageNames(namespace, model.getNextPage());
				logPageNames(data, iteration);

				// retrieve next names if exists
				while (model.getNextPage() != null) {
					iteration++;

					// check pause beetween query
					processRunManager.pause();

					// logs
					if (model.getNextPage() != null) {
						logger.appendLog("Récupération des noms de page...");
					} else {
						logger.appendLog("Récupération des noms de page (depuis : " + model.getNextPage() + ") ...");
					}

					// check interruption
					this.processRunManager.checkInterruption();

					// pause between each query
					if (model.getNextPage().length() > 0 && model.getQueryPauseTime() > 0) {
						logger.appendLog("pause (" + model.getQueryPauseTime() + "ms)...");
						Thread.sleep(model.getQueryPauseTime());
					}

					// next call wiki web service
					data = wikiService.retrievePageNames(namespace, model.getNextPage());
					logPageNames(data, iteration);
				}

				// logs
				if (model.countNames(namespace) > 0) {
					ExportData first = model.getFirstExportData(namespace);
					ExportData last = model.getLastExportData(namespace);
					logger.appendProgress("&nbsp;&nbsp;&nbsp;&nbsp;#" + first.getId()
							+ (last == first ? "" : ("-" + model.getLastExportData(namespace).getId()))
							+ " - <i>Noms de page récupérés</i> : <b style=\"color: #00ff00;\">"
							+ model.countNames(namespace) + "</b><br>");
				} else {
					logger.appendProgress(
							"&nbsp;&nbsp;&nbsp;&nbsp;Noms de page récupérés</i> : <b style=\"color: #00ff00;\">0</b><br>");
				}
				model.setWikiNamespaceReadStatus(namespace, TaskStatus.DONE_SUCCESS);
			}
		} catch (InterruptedException | AbortProcessException e) {
			logger.appendProgress(
					"&nbsp;&nbsp;&nbsp;&nbsp;<b style=\"color: #ff0000;\">Echec<br><br>Processus interrompu.</b><br>");
			model.setCurrenProcessState(TaskStatus.DONE_FAILED);
		}
		logger.appendLog("Nombre de noms de page récupérés : " + model.countNames());
		model.setEndDate(new Date());
		if (model.getCurrenProcessState() == TaskStatus.PROCESSING) {
			model.setCurrenProcessState(TaskStatus.DONE_SUCCESS);
		}
		model.setApplicationState(AppState.GET_NAMES_DONE);
	}

	private void logPageNames(ExportData data, int iteration) {
		if (data == null) {
			return;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("----------- #" + data.getId() + " - " + data.getNamespace() + " (" + iteration
				+ ") BEGIN -------------");
		sb.append(SKIP_LINE);
		for (String pageName : data.getPageNames()) {
			sb.append(pageName);
			sb.append(SKIP_LINE);
		}
		sb.append("----------- #" + data.getId() + " - " + data.getNamespace() + " (" + iteration
				+ ") END -------------");
		sb.append(SKIP_LINE);
		logger.appendLog(sb.toString());
	}

	/**
	 * Export Wiki pages from a page name list.
	 */
	@Override
	public void performExportPages() {
		model.setStartDate(new Date());
		model.getRetryList().clear();
		model.setCurrenProcessState(TaskStatus.PROCESSING);
		model.setApplicationState(AppState.EXPORT_PAGES_PROCESSING);
		model.getExportDataList().forEach(data -> {
			data.setExportXML(null);
			model.setExportStatus(data, TaskStatus.PROCESSING);
		});
		
		logger.appendProgress(
				"<span style=\"color: orange; font-weight: bold;\">-----------------------------------<br>Export des pages<br>-----------------------------------</span>");

		try {
			// first export attempts
			WikiNamespace currentNamespace = null;
			for (ExportData exportData : model.getExportDataList()) {
				// select current Namespace Data
				if (currentNamespace == null) {
					currentNamespace = exportData.getNamespace();
					model.setWikiNamespaceExportStatus(currentNamespace, TaskStatus.PROCESSING);
				} else if (exportData.getNamespace() != currentNamespace) {
					finalizeNamespaceExportStatus(currentNamespace, model.getWikiNamespaceData(currentNamespace).getExportDataList(), TaskStatus.WARNING);
					currentNamespace = exportData.getNamespace();
					model.setWikiNamespaceExportStatus(currentNamespace, TaskStatus.PROCESSING);
				}
				
				// check interruption
				this.processRunManager.checkInterruption();

				// call wiki web service
				wikiService.performExportPages(exportData);
				if (exportData.getStatus() == TaskStatus.DONE_FAILED) {
					model.addRetry(exportData);
				}
			}
			finalizeNamespaceExportStatus(currentNamespace, model.getWikiNamespaceData(currentNamespace).getExportDataList(), TaskStatus.WARNING);

			// retrying failed attempts => resizing request
			if (model.getRetryList().size() > 0) {
				logger.appendProgress(
						"<span style=\"color: orange; font-weight: bold;\">-----------------------------------<br>Seconde tentatives<br>-----------------------------------</span>");
				
				currentNamespace = null;
				for (ExportData exportData : model.getRetryList()) {
					// select current Namespace Data
					if (currentNamespace == null) {
						currentNamespace = exportData.getNamespace();
						model.setWikiNamespaceExportStatus(currentNamespace, TaskStatus.PROCESSING);
					} else if (exportData.getNamespace() != currentNamespace) {
						finalizeNamespaceExportStatus(currentNamespace, model.getRetryList(), TaskStatus.DONE_FAILED);
						currentNamespace = exportData.getNamespace();
						model.setWikiNamespaceExportStatus(currentNamespace, TaskStatus.PROCESSING);
					}
					
					// check interruption
					this.processRunManager.checkInterruption();

					// call wiki web service
					wikiService.performExportPages(exportData);
					if (exportData.getStatus() == TaskStatus.DONE_FAILED) {
						model.setCurrenProcessState(TaskStatus.DONE_FAILED);
					}
				}
				finalizeNamespaceExportStatus(currentNamespace, model.getRetryList(), TaskStatus.DONE_FAILED);
			}
		} catch (InterruptedException | AbortProcessException e) {
			logger.appendProgress(
					"&nbsp;&nbsp;&nbsp;&nbsp;<b style=\"color: #ff0000;\">Echec<br><br>Processus interrompu.</b><br>");
			model.setCurrenProcessState(TaskStatus.DONE_FAILED);
		}

		model.setEndDate(new Date());
		if (model.getCurrenProcessState() == TaskStatus.PROCESSING) {
			model.setCurrenProcessState(TaskStatus.DONE_SUCCESS);
		}
		model.setApplicationState(AppState.EXPORT_PAGES_DONE);
	}

	private void finalizeNamespaceExportStatus(WikiNamespace namespace, List<ExportData> dataList, TaskStatus failStatus) {
		if (dataList != null && dataList.size() > 0) {
			TaskStatus status = TaskStatus.DONE_SUCCESS;
			for (ExportData subData : dataList) {
				if (subData.getNamespace() == namespace && subData.getStatus() != TaskStatus.DONE_SUCCESS) {
					status = failStatus;
					break;
				}
			}
			model.setWikiNamespaceExportStatus(namespace, status);
		}
	}

	/**
	 * Store export data into a ZIP Archive.
	 */
	@Override
	public void performSaveXMLFiles() {

		model.setCurrenProcessState(TaskStatus.PROCESSING);
		model.setApplicationState(AppState.SAVE_XML_FILES_PROCESSING);

		// data available to storage
		List<ExportData> dataList = new ArrayList<>();
		for (ExportData data : model.getExportDataList()) {
			dataList.add(data);
		}
		for (ExportData data : model.getRetryList()) {
			dataList.add(data);
		}

		// choose target ZIP file
		File zipFile = fileHelper.openSaveFileWindow(view, new ZipFileFilter());
		if (zipFile == null) {
			// no target file
			model.setCurrenProcessState(TaskStatus.UNDEFINED);
			model.setApplicationState(AppState.SAVE_XML_FILES_DONE);
			return;
		}

		// build and store data into the ZIP archive
		if (fileHelper.storeExportsAsZipArchive(dataList, zipFile)) {
			model.setExportsDataFile(zipFile);
			model.setCurrenProcessState(TaskStatus.DONE_SUCCESS);
			model.setApplicationState(AppState.SAVE_XML_FILES_DONE);
		} else {
			model.setCurrenProcessState(TaskStatus.DONE_FAILED);
			model.setApplicationState(AppState.SAVE_XML_FILES_DONE);
		}

	}

	@Override
	public void performSaveNamesOnlyFiles() {

		model.setCurrenProcessState(TaskStatus.PROCESSING);
		model.setApplicationState(AppState.SAVE_NAMES_FILES_PROCESSING);

		// choose target ZIP file
		File zipFile = fileHelper.openSaveFileWindow(view, new ZipFileFilter());
		if (zipFile == null) {
			// no target file
			model.setCurrenProcessState(TaskStatus.UNDEFINED);
			model.setApplicationState(AppState.SAVE_NAMES_FILES_DONE);
			return;
		}

		// build and store data into the ZIP archive
		if (fileHelper.storeNamesAsZipArchive(model.getExportDataList(), zipFile)) {
			model.setExportsDataFile(zipFile);
			model.setCurrenProcessState(TaskStatus.DONE_SUCCESS);
			model.setApplicationState(AppState.SAVE_NAMES_FILES_DONE);
		} else {
			model.setCurrenProcessState(TaskStatus.DONE_FAILED);
			model.setApplicationState(AppState.SAVE_NAMES_FILES_DONE);
		}

	}

	@Override
	public void stopCurrentProcess() {
		model.setCurrenProcessState(TaskStatus.INTERRUPTING);
	}
}
