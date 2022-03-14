package com.savemywiki.mediawiki.client;

import java.io.IOException;
import java.net.URISyntaxException;

import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.WikiNamespace;

public interface IMediawikiClient {

	/**
	 * Send a HTTP Request to retrieve page names from a web site using Mediawiki
	 * framework.
	 * 
	 * @return HTTPWikiResponse
	 */
	HTTPWikiResponse listPageNamesRequest(WikiNamespace namespace, String fromPageName)
			throws URISyntaxException, IOException, InterruptedException;

	/**
	 * Send a HTTP Request to export page data from a web site using Mediawiki
	 * framework.
	 * 
	 * @return HTTPWikiResponse
	 */
	HTTPWikiResponse exportRequest(ExportData exportData) throws URISyntaxException, IOException, InterruptedException;

}