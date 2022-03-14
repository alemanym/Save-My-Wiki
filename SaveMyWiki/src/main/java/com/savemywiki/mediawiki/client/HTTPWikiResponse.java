package com.savemywiki.mediawiki.client;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

public class HTTPWikiResponse {

	private int statusCode;
	private String body;

	public HTTPWikiResponse(CloseableHttpResponse response) {

		this.statusCode = response.getStatusLine().getStatusCode();
		try {
			this.body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("=============================");
		System.out.println(body);
		System.out.println("=============================");
	}
	
	public HTTPWikiResponse(HttpResponse<String> response) {
		this.statusCode = response.statusCode();
		this.body = response.body();
	}

	public int statusCode() {
		return statusCode;
	}

	public String body() {
		return body;
	}

}
