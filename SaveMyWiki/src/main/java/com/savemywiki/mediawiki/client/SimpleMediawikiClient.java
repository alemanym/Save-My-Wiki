package com.savemywiki.mediawiki.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.apache.http.client.utils.URIBuilder;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.WikiNamespace;
import com.savemywiki.utils.Logger;

/**
 * Client to call Mediawiki web service.
 */
public class SimpleMediawikiClient implements IMediawikiClient {

	private static final String RELATIVE_URL_EXPORT = "/index.php/Sp%C3%A9cial:Exporter";
	private static final String RELATIVE_URL_API = "/api.php";
	private static final String SKIP_LINE = "\r\n";

	private HttpClient httpClient;
	private AppModel model;
	private Logger logger;

	public SimpleMediawikiClient(AppModel model, Logger logger) {
		this.model = model;
		this.logger = logger;
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(1)).build();
	}

	/**
	 * Send a HTTP Request to retrieve page names from a web site using Mediawiki
	 * framework.
	 */
	@Override
	public HTTPWikiResponse listPageNamesRequest(WikiNamespace namespace, String fromPageName)
			throws URISyntaxException, IOException, InterruptedException {
		URI uri = buildListPageURI(model, namespace, fromPageName);
		logger.appendLog(uri.toString());

		HttpRequest request = HttpRequest.newBuilder().uri(uri).headers("Content-Type", "text/plain;charset=UTF-8")
				.POST(HttpRequest.BodyPublishers.noBody()).build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

		return new HTTPWikiResponse(response);
	}

	/**
	 * Build URI of the retrieve page names request.
	 */
	private URI buildListPageURI(AppModel model, WikiNamespace namespace, String fromPageName)
			throws URISyntaxException {
		URIBuilder b = new URIBuilder(model.getWebsiteURL() + RELATIVE_URL_API);
		b.addParameter("action", "query");
		b.addParameter("list", "allpages");
		b.addParameter("format", "json");
		b.addParameter("aplimit", "" + model.getNamesQueryLimit());
		b.addParameter("apfrom", fromPageName);
		b.addParameter("exportnowrap", "1");
		b.addParameter("apnamespace", namespace.getId());
		URI uri = b.build();
		return uri;
	}

	/**
	 * Send a HTTP Request to export page data from a web site using Mediawiki
	 * framework.
	 */
	@Override
	public HTTPWikiResponse exportRequest(ExportData exportData)
			throws URISyntaxException, IOException, InterruptedException {
		URI uri = buildExportURI(model, exportData);
		logger.appendLog(uri.toString());

		HttpRequest request = HttpRequest.newBuilder().uri(uri)
				.headers("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.noBody())
				.build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

		return new HTTPWikiResponse(response);
	}

	/**
	 * Build URI of the export pages request.
	 */
	private URI buildExportURI(AppModel model, ExportData exportData) throws URISyntaxException {

		// build query parameter
		StringBuffer pagesParam = new StringBuffer();

		for (String pageName : exportData.getPageNames()) {
			pagesParam.append(pageName);
			pagesParam.append(SKIP_LINE);
		}

		URIBuilder b = new URIBuilder(model.getWebsiteURL() + RELATIVE_URL_EXPORT);
		b.addParameter("pages", pagesParam.toString()); // liste des noms de pages
		b.addParameter("curonly", "1"); // dernière version de chaque page seulement
		b.addParameter("wpDownload", "1"); // download data
		URI uri = b.build();
		return uri;
	}
}
