package com.janilla.petclinic.frontend;

import java.net.URI;
import java.util.List;
import java.util.Properties;

import com.janilla.http.HttpClient;
import com.janilla.http.HttpRequest;
import com.janilla.java.Converter;
import com.janilla.java.SimpleParameterizedType;
import com.janilla.java.UriQueryBuilder;
import com.janilla.persistence.ListPortion;
import com.janilla.petclinic.Vet;
import com.janilla.petclinic.VetApi;

public class FrontendVetApi implements VetApi {

	protected final Properties configuration;

	protected final HttpClient httpClient;

	public FrontendVetApi(Properties configuration, HttpClient httpClient) {
		this.configuration = configuration;
		this.httpClient = httpClient;
	}

	@Override
	public ListPortion<Vet> read(Integer depth, Integer skip, Integer limit) {
		var u = URI.create(configuration.getProperty("petclinic.api.url") + "/vets?"
				+ new UriQueryBuilder().append("depth", depth != null ? depth.toString() : null)
						.append("skip", skip != null ? skip.toString() : null)
						.append("limit", limit != null ? limit.toString() : null));
		var o = httpClient.send(new HttpRequest("GET", u), HttpClient.JSON);
		return new Converter().convert(o, new SimpleParameterizedType(ListPortion.class, List.of(Vet.class)));
	}
}
