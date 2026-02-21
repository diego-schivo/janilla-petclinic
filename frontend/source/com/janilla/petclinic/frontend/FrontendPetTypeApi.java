package com.janilla.petclinic.frontend;

import java.net.URI;
import java.util.List;
import java.util.Properties;

import com.janilla.http.HttpClient;
import com.janilla.http.HttpRequest;
import com.janilla.java.Converter;
import com.janilla.java.SimpleParameterizedType;
import com.janilla.petclinic.PetType;
import com.janilla.petclinic.PetTypeApi;

public class FrontendPetTypeApi implements PetTypeApi {

	protected final Properties configuration;

	protected final HttpClient httpClient;

	public FrontendPetTypeApi(Properties configuration, HttpClient httpClient) {
		this.configuration = configuration;
		this.httpClient = httpClient;
	}

	@Override
	public List<PetType> read() {
		var u = URI.create(configuration.getProperty("petclinic.api.url") + "/pet-types");
		var o = httpClient.send(new HttpRequest("GET", u), HttpClient.JSON);
		return new Converter().convert(o, new SimpleParameterizedType(List.class, List.of(PetType.class)));
	}
}
