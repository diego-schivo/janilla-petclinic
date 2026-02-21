package com.janilla.petclinic.frontend;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.Properties;

import com.janilla.http.HttpClient;
import com.janilla.http.HttpRequest;
import com.janilla.java.Converter;
import com.janilla.java.UriQueryBuilder;
import com.janilla.json.Json;
import com.janilla.petclinic.Pet;
import com.janilla.petclinic.PetApi;

public class FrontendPetApi implements PetApi {

	protected final Properties configuration;

	protected final HttpClient httpClient;

	public FrontendPetApi(Properties configuration, HttpClient httpClient) {
		this.configuration = configuration;
		this.httpClient = httpClient;
	}

	@Override
	public Pet create(Pet pet) {
		var rq = new HttpRequest("POST", URI.create(configuration.getProperty("petclinic.api.url") + "/pets"));
		rq.setHeaderValue("content-type", "application/json");
		rq.setBody(Channels.newChannel(new ByteArrayInputStream(Json.format(pet, true).getBytes())));
		var o = httpClient.send(rq, HttpClient.JSON);
		return new Converter().convert(o, Pet.class);
	}

	@Override
	public Pet read(Long id, Integer depth) {
		var u = URI.create(configuration.getProperty("petclinic.api.url") + "/pets/" + id + "?"
				+ new UriQueryBuilder().append("depth", depth != null ? depth.toString() : null));
		var o = httpClient.send(new HttpRequest("GET", u), HttpClient.JSON);
		return new Converter().convert(o, Pet.class);
	}

	@Override
	public Pet update(Long id, Pet pet) {
//		IO.println("FrontendPetApi.update, id=" + id + ", pet=" + pet);
		var rq = new HttpRequest("PUT", URI.create(configuration.getProperty("petclinic.api.url") + "/pets/" + id));
		rq.setHeaderValue("content-type", "application/json");
		rq.setBody(Channels.newChannel(new ByteArrayInputStream(Json.format(pet, true).getBytes())));
		var o = httpClient.send(rq, HttpClient.JSON);
		return new Converter().convert(o, Pet.class);
	}
}
