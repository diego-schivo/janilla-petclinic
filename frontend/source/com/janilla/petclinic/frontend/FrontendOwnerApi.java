package com.janilla.petclinic.frontend;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Properties;

import com.janilla.http.HttpClient;
import com.janilla.http.HttpRequest;
import com.janilla.java.Converter;
import com.janilla.java.SimpleParameterizedType;
import com.janilla.java.UriQueryBuilder;
import com.janilla.json.Json;
import com.janilla.persistence.ListPortion;
import com.janilla.petclinic.Owner;
import com.janilla.petclinic.OwnerApi;

public class FrontendOwnerApi implements OwnerApi {

	protected final Properties configuration;

	protected final HttpClient httpClient;

	public FrontendOwnerApi(Properties configuration, HttpClient httpClient) {
		this.configuration = configuration;
		this.httpClient = httpClient;
	}

	@Override
	public Owner create(Owner owner) {
		var rq = new HttpRequest("POST", URI.create(configuration.getProperty("petclinic.api.url") + "/owners"));
		rq.setHeaderValue("content-type", "application/json");
		rq.setBody(Channels.newChannel(new ByteArrayInputStream(Json.format(owner, true).getBytes())));
		var o = httpClient.send(rq, HttpClient.JSON);
		return new Converter().convert(o, Owner.class);
	}

	@Override
	public ListPortion<Owner> read(String lastName, Integer depth, Integer skip, Integer limit) {
		var u = URI.create(configuration.getProperty("petclinic.api.url") + "/owners?"
				+ new UriQueryBuilder().append("lastName", lastName)
						.append("depth", depth != null ? depth.toString() : null)
						.append("skip", skip != null ? skip.toString() : null)
						.append("limit", limit != null ? limit.toString() : null));
		var o = httpClient.send(new HttpRequest("GET", u), HttpClient.JSON);
		return new Converter().convert(o, new SimpleParameterizedType(ListPortion.class, List.of(Owner.class)));
	}

	@Override
	public Owner read(Long id, Integer depth) {
		var u = URI.create(configuration.getProperty("petclinic.api.url") + "/owners/" + id + "?"
				+ new UriQueryBuilder().append("depth", depth != null ? depth.toString() : null));
		var o = httpClient.send(new HttpRequest("GET", u), HttpClient.JSON);
		return new Converter().convert(o, Owner.class);
	}

	@Override
	public Owner update(Long id, Owner owner) {
		IO.println("FrontendOwnerApi.update, id=" + id + ", owner=" + owner);
		var rq = new HttpRequest("PUT", URI.create(configuration.getProperty("petclinic.api.url") + "/owners/" + id));
		rq.setHeaderValue("content-type", "application/json");
		rq.setBody(Channels.newChannel(new ByteArrayInputStream(Json.format(owner, true).getBytes())));
		var o = httpClient.send(rq, HttpClient.JSON);
		return new Converter().convert(o, Owner.class);
	}
}
