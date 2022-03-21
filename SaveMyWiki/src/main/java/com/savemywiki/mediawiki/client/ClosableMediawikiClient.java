package com.savemywiki.mediawiki.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.WikiNamespace;
import com.savemywiki.utils.Logger;

/**
 * Client to call Mediawiki web service.
 */
public class ClosableMediawikiClient implements IMediawikiClient {

	private static final String RELATIVE_URL_EXPORT = "/index.php/Sp%C3%A9cial:Exporter";
	private static final String RELATIVE_URL_API = "/api.php";
	private static final String SKIP_LINE = "\r\n";
	private static final int TIME_OUT_SEC = 30;
	private CloseableHttpClient httpClient;
	private AppModel model;
	private Logger logger;

	public ClosableMediawikiClient(AppModel model, Logger logger) {
		httpClient = buildClient();
	}

	private CloseableHttpClient buildClient() {
//	    final PoolingHttpClientConnectionManager SocketConfig socketConfig = SocketConfig.custom()
//	            .setSoKeepAlive(Boolean.TRUE)
//	            .setTcpNoDelay(Boolean.TRUE)
//	            .setSoTimeout(60 * 1000).build();
//	    
//	    cm = new PoolingHttpClientConnectionManager();
//	    cm.setDefaultMaxPerRoute(10);
//	    cm.setMaxTotal(10);
//	    cm.setDefaultSocketConfig(socketConfig);
//	    
//	    final RequestConfig requestConfig = RequestConfig.custom()
//				.setCookieSpec(CookieSpecs.STANDARD)
//				.setConnectTimeout(20 * 1000)
//				.setSocketTimeout(60 * 1000)
//				.setConnectionRequestTimeout(60 * 1000).build();

		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(TIME_OUT_SEC * 1000)
				.setConnectionRequestTimeout(TIME_OUT_SEC * 1000)
				.setSocketTimeout(TIME_OUT_SEC * 1000).build();
		return HttpClientBuilder.create().setDefaultRequestConfig(config).build();

//		return HttpClients.custom()
//				.setConnectionManager(cm)
//				.setDefaultRequestConfig(requestConfig)
//	            .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
//				.setConnectionBackoffStrategy(new NullBackoffStrategy())
//				.setRetryHandler(new DefaultHttpRequestRetryHandler(10, true))
//	            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
//	            .disableAutomaticRetries()
//				.build();

//		return HttpClients.createDefault();
	}

	/**
	 * Send a HTTP Request to retrieve page names from a web site using Mediawiki
	 * framework.
	 * 
	 * @return
	 */
	@Override
	public HTTPWikiResponse listPageNamesRequest(WikiNamespace namespace, String fromPageName)
			throws URISyntaxException, IOException, InterruptedException {

		URI uri = buildListPageURI(model, namespace, fromPageName);
		logger.appendLog(uri.toString());

		final HttpPost request = new HttpPost(uri);
		request.setHeader("Content-Type", "text/plain;charset=UTF-8");

		HTTPWikiResponse resp;
		httpClient = buildClient();
		try (CloseableHttpResponse response = httpClient.execute(request)) {
			resp = new HTTPWikiResponse(response);
			request.abort();
		}
		httpClient.close();

		return resp;
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

		final HttpPost request = new HttpPost(uri);
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");

		HTTPWikiResponse resp;
		try (CloseableHttpResponse response = httpClient.execute(request)) {
			resp = new HTTPWikiResponse(response);
		}

		return resp;
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

	@Override
	protected void finalize() throws Throwable {
		httpClient.close();
	}
}
