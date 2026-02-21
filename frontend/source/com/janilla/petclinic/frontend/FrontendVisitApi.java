package com.janilla.petclinic.frontend;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.Properties;

import com.janilla.http.HttpClient;
import com.janilla.http.HttpRequest;
import com.janilla.java.Converter;
import com.janilla.json.Json;
import com.janilla.petclinic.Visit;
import com.janilla.petclinic.VisitApi;

public class FrontendVisitApi implements VisitApi {

	protected final Properties configuration;

	protected final HttpClient httpClient;

	public FrontendVisitApi(Properties configuration, HttpClient httpClient) {
		this.configuration = configuration;
		this.httpClient = httpClient;
	}

	@Override
	public Visit create(Visit visit) {
		var rq = new HttpRequest("POST", URI.create(configuration.getProperty("petclinic.api.url") + "/visits"));
		rq.setHeaderValue("content-type", "application/json");
		rq.setBody(Channels.newChannel(new ByteArrayInputStream(Json.format(visit, true).getBytes())));
		var o = httpClient.send(rq, HttpClient.JSON);
		return new Converter().convert(o, Visit.class);
	}
}
