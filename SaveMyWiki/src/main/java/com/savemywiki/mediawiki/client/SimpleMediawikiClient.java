package com.savemywiki.mediawiki.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.WikiNamespace;
import com.savemywiki.utils.Logger;

/**
 * Client to call Mediawiki web service API.
 * 
 * @author Marc Alemany
 */
public class SimpleMediawikiClient implements IMediawikiClient {

	private static final String RELATIVE_URL_API = "/api.php";
	private static final String PAGE_SEPARATOR = "|";
	private static final int TIMEOUT_MILLISEC = 30000;

	private AppModel model;
	private Logger logger;

	public SimpleMediawikiClient(AppModel model, Logger logger) {
		this.model = model;
		this.logger = logger;
	}

	/**
	 * Send a HTTP Request to retrieve page names from a web site using Mediawiki
	 * framework.
	 */
	@Override
	public HTTPWikiResponse listPageNamesRequest(WikiNamespace namespace, String fromPageName)
			throws URISyntaxException, IOException, InterruptedException {
		
		// init
		HttpURLConnection con = initListePagesRequest(model, namespace, fromPageName);

		// send
		int status = con.getResponseCode();
		
		// response
		return readResponse(con, status);
	}

	private HttpURLConnection initListePagesRequest(AppModel model, WikiNamespace namespace, String fromPageName) throws IOException {
		
		// query parameters
		Map<String, String> parameters = new HashMap<>();
		parameters.put("param1", "val");
		parameters.put("action", "query");
		parameters.put("list", "allpages");
		parameters.put("format", "json");
		parameters.put("aplimit", "" + model.getNamesQueryLimit());
		parameters.put("apfrom", fromPageName);
		parameters.put("exportnowrap", "1");
		parameters.put("apnamespace", namespace.getId());

		// connection init
		String method = "POST";
		String uri = model.getWebsiteURL() + RELATIVE_URL_API + "?" + ParameterStringBuilder.getParamsString(parameters);
		URL url = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(method);
		logger.appendLog("Request : " + method + " " + uri);
		
		// header
		con.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
		con.setRequestProperty("Accept", "application/json");
		
		// timeouts
		con.setConnectTimeout(TIMEOUT_MILLISEC);
		con.setReadTimeout(TIMEOUT_MILLISEC);

		con.setDoOutput(true);
		
		return con;
	}

	/**
	 * Send a HTTP Request to export page data from a web site using Mediawiki
	 * framework.
	 * @throws IOException 
	 */
	@Override
	public HTTPWikiResponse exportRequest(ExportData exportData) throws IOException {

		// init
		HttpURLConnection con = initExportRequest(model, exportData);

		// send
		int status = con.getResponseCode();
		
		// response
		return readResponse(con, status);
	}

	private HttpURLConnection initExportRequest(AppModel model, ExportData exportData) throws IOException {
		// query parameters
		StringBuffer pagesParam = new StringBuffer();
		for (String pageName : exportData.getPageNames()) {
			pagesParam.append(pageName);
			pagesParam.append(PAGE_SEPARATOR);
		}
		Map<String, String> parameters = new HashMap<>();
		parameters.put("action", "query");
		parameters.put("format", "json");
		parameters.put("prop", "revisions");
		parameters.put("export", "1");
		parameters.put("exportnowrap", "1");
		parameters.put("titles", pagesParam.toString()); // liste des noms de pages

		// connection init
		String method = "POST";
		String uri = model.getWebsiteURL() + RELATIVE_URL_API + "?" + ParameterStringBuilder.getParamsString(parameters);
		URL url = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(method);
		logger.appendLog("Request : " + method + " " + uri);
		
		// method
		con.setRequestMethod("GET");
		
		// header
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		// timeouts
		con.setConnectTimeout(TIMEOUT_MILLISEC);
		con.setReadTimeout(TIMEOUT_MILLISEC);

		con.setDoOutput(true);
		
		return con;
	}

	private HTTPWikiResponse readResponse(HttpURLConnection con, int status) throws IOException {
		try(BufferedReader in = new BufferedReader(
				  new InputStreamReader(con.getInputStream()))) {
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			    content.append("\r\n");
			}
			return new HTTPWikiResponse(status, content.toString());
		} finally {
			con.disconnect();
		}
	}

	/*
	private String authToken;
	private void authenticate() throws IOException, InterruptedException, URISyntaxException {

		// request params
		URIBuilder b = new URIBuilder(model.getWebsiteURL() + RELATIVE_URL_API);
		b.addParameter("action", "query");
		b.addParameter("format", "json");
		b.addParameter("meta", "tokens");
		b.addParameter("type", "login");
		URI uri = b.build();

		logger.appendLog(uri.toString());

		// call web service
		HttpRequest request = HttpRequest.newBuilder().uri(uri).headers("Content-Type", "text/plain;charset=UTF-8")
				.POST(HttpRequest.BodyPublishers.noBody()).build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

		// pages response
		JSONObject data = new JSONObject(response.body());
		JSONArray pagesData = null;
		try {
			this.authToken = (String) data.getJSONObject("query").getJSONObject("tokens").get("logintoken");
		} catch (JSONException e) {
			throw e;
		}
	}
	*/
}
