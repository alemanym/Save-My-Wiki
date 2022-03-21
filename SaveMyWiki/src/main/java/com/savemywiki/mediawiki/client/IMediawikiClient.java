package com.savemywiki.mediawiki.client;

import java.io.IOException;
import java.net.URISyntaxException;

import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.WikiNamespace;

/**
 * Interface of Client to call Mediawiki web service API.
 * 
 * @author Marc Alemany
 */
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