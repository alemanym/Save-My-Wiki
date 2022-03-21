package com.savemywiki.mediawiki.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.savemywiki.mediawiki.client.HTTPWikiResponse;
import com.savemywiki.mediawiki.client.SimpleMediawikiClient;
import com.savemywiki.tools.export.ctrl.ProcessRunManager;
import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.TaskStatus;
import com.savemywiki.tools.export.model.WikiNamespace;
import com.savemywiki.utils.AbortProcessException;
import com.savemywiki.utils.Logger;

public class MediawikiService {

	private SimpleMediawikiClient wikiClient;
	private AppModel model;
	private Logger logger;
	private ProcessRunManager processManager;

	public MediawikiService(AppModel model, ProcessRunManager processManager, Logger logger) {
		this.logger = logger;
		this.model = model;
		this.processManager = processManager;
//		wikiClient = new ClosableMediawikiClient(model, logger);
		wikiClient = new SimpleMediawikiClient(model, logger);
	}

	/**
	 * Retrieve wiki page names of a given WikiNamespace and from a given page name.
	 * 
	 * @param namespace    WikiNamespace - type of wiki pages
	 * @param fromPageName String Name of the first page to retrieve
	 * @return 
	 * @throws AbortProcessException
	 */
	public ExportData retrievePageNames(WikiNamespace namespace, String fromPageName)
			throws InterruptedException, AbortProcessException {
		return retrievePageNamesAlt(namespace, fromPageName, 5);
	}

	private ExportData retrievePageNamesAlt(WikiNamespace namespace, String fromPageName, int retryCount) throws InterruptedException, AbortProcessException {
		try {
			processManager.pause();
			
			// call wiki web service
			HTTPWikiResponse response = wikiClient.listPageNamesRequest(namespace, fromPageName);

			if (response.statusCode() != 200) {
				// Response : Error
				model.setCurrenProcessState(TaskStatus.DONE_FAILED);
				logger.appendLog(beautify(response.body()));
				logger.appendProgress("&nbsp;&nbsp;&nbsp;&nbsp;<b style=\"color: #ff0000;\">Echec : erreur HTTP "
						+ response.statusCode() + "</b>");
			} else {
				// Response : OK => data parsing
				return parsePageNames(response.body(), namespace);
			}
		} catch (URISyntaxException | IOException | InterruptedException e) {
			// Fatal error
			model.setNextPage(null);
			model.setCurrenProcessState(TaskStatus.DONE_FAILED);
			logger.appendLog(e.toString()+ "\r\n");
			e.printStackTrace();
			
			// check retry
			return retryRetrievePageNames(namespace, fromPageName, retryCount, e);
		}
		return null;
	}

	private ExportData retryRetrievePageNames(WikiNamespace namespace, String fromPageName, int retryCount, Exception e)
			throws InterruptedException, AbortProcessException {
		if(retryCount > 0) {
			logger.appendProgress(
					"&nbsp;&nbsp;&nbsp;&nbsp;<b style=\"color: orange;\">Echec : erreur "
							+ e.getClass().getSimpleName() + ", cause= \"" + e.getMessage() + "\" </b><br>Nouvelle tentative...");
			
			// temporize
			processManager.pause(200);
			
			retryCount--;
			return retrievePageNamesAlt(namespace, fromPageName, retryCount);
		} else {
			logger.appendProgress(
					"&nbsp;&nbsp;&nbsp;&nbsp;<b style=\"color: red;\">Echec fatal : erreur "
							+ e.getClass().getSimpleName() + ", cause= \"" + e.getMessage() + "\" </b>");
			return null;
		}
	}

	/**
	 * Parse list page names from HTTP Response.
	 */
	private ExportData parsePageNames(String json, WikiNamespace namespace) {
		// pages data
		JSONObject data = new JSONObject(json);
		JSONArray pagesData = null;
		try {
			pagesData = data.getJSONObject("query").getJSONArray("allpages");
		} catch (JSONException e) {
			logger.appendLog("Nombre de noms récupérés : 0");
			model.setNextPage(null);
			return null;
		}

		if (pagesData.length() == 0) {
			logger.appendLog("Nombre de noms récupérés : 0");
			model.setNextPage(null);
			return null;
		}

		ExportData dataModel = new ExportData(namespace);
		dataModel.setId(model.getExportDataList().size() + 1);
		for (Object pageObj : pagesData) {
			JSONObject pageJson = (JSONObject) pageObj;
			dataModel.getPageNames().add((String) pageJson.get("title"));
		}
		model.add(dataModel);
		logger.appendLog("Nombre de noms récupérés : " + pagesData.length());

		// next page
		try {
			String nextPage = (String) data.getJSONObject("continue").get("apcontinue");
			model.setNextPage(nextPage);
			logger.appendLog("Page suivante : " + model.getNextPage() + "\r\n");
		} catch (JSONException e) {
			// no next page => end of list
			model.setNextPage(null);
			logger.appendLog("Page suivante : aucune\r\n");
		}

		return dataModel;
	}

	public void performExportPages(ExportData exportData) throws InterruptedException, AbortProcessException {
		// export status
		model.setExportStatus(exportData, TaskStatus.PROCESSING);

		// logs
		if (!exportData.isRetry()) {
			logger.appendProgress("\r\n[" + model.fromStartDate() + "] #" + exportData.getId()
					+ " - <b style=\"color: white;\">" + exportData.getPageNames().size()
					+ "</b> pages de type <b style=\"color: white;\">" + exportData.getNamespace().desc() + "</b>");
		} else {
			logger.appendProgress("\r\n[" + model.fromStartDate() + "] #" + exportData.getRetrySource().getId()
					+ " - <b style=\"color: white;\">" + exportData.getPageNames().size()
					+ "</b> pages de type <b style=\"color: white;\">" + exportData.getNamespace().desc()
					+ "</b> (2e essai)");
		}
		logger.appendLog("[" + model.fromStartDate() + "] Requête - " + exportData.getId());
		
		try {

			// call wiki web service
			HTTPWikiResponse response = wikiClient.exportRequest(exportData);

			if (response.statusCode() != 200) {
				// Response : Error
				model.setExportStatus(exportData, TaskStatus.DONE_FAILED);
				System.err.println(response.toString());
			} else {
				// Response : OK => store XML
				model.setExportStatus(exportData, TaskStatus.DONE_SUCCESS);
				exportData.setExportXML(response.body());
			}
		} catch (IOException e) {
			// fatal error
			model.setExportStatus(exportData, TaskStatus.DONE_FAILED);
			e.printStackTrace();
		} finally {
			// logs
			if (exportData.getStatus() == TaskStatus.DONE_SUCCESS) {
				logger.appendLog("[" + model.fromStartDate() + "] => OK\n\r");
				logger.appendProgress("&nbsp;&nbsp;&nbsp;&nbsp;<i>Export</i> : <b style=\"color: #00ff00;\">OK</b><br>");
			} else {
				logger.appendLog("[" + model.fromStartDate() + "] => ECHEC (à refaire en baissant le nombre de page)\n\r");
				if (!exportData.isRetry()) {
					logger.appendProgress(
							"&nbsp;&nbsp;&nbsp;&nbsp;<i>Export</i> : <b style=\"color: orange;\">A Refaire</b><br>");
				} else {
					logger.appendProgress(
							"&nbsp;&nbsp;&nbsp;&nbsp;<i>Export</i> : <b style=\"color: #ff0000;\">Echec</b><br>");
				}
			}

			if (model.getQueryPauseTime() > 0) {
				logger.appendLog("[" + model.fromStartDate() + "] pause (" + model.getQueryPauseTime() + "ms)...");
				Thread.sleep(model.getQueryPauseTime());
			}
		}
	}

	private String beautify(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Object obj = mapper.readValue(json, Object.class);
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
	}
}
