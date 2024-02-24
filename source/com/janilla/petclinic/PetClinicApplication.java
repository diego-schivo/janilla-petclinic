/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.janilla.petclinic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Supplier;

import com.janilla.http.HttpExchange;
import com.janilla.http.HttpServer;
import com.janilla.io.IO;
import com.janilla.persistence.Persistence;
import com.janilla.util.Lazy;
import com.janilla.web.ApplicationHandlerBuilder;

/**
 * @author Dave Syer
 * @author Diego Schivo
 */
public class PetClinicApplication {

	public static void main(String[] args) throws IOException {
		var a = new PetClinicApplication();
		{
			var c = new Properties();
			try (var s = a.getClass().getResourceAsStream("configuration.properties")) {
				c.load(s);
			}
			a.setConfiguration(c);
		}
		a.getPersistence();

		var s = new HttpServer();
		s.setPort(Integer.parseInt(a.getConfiguration().getProperty("petclinic.server.port")));
		s.setHandler(a.getHandler());
		s.run();
	}

	Properties configuration;

	private IO.Supplier<Persistence> persistence = IO.Lazy.of(() -> {
		var b = new CustomPersistenceBuilder();
		{
			var p = configuration.getProperty("petclinic.database.path");
			if (p.startsWith("~"))
				p = System.getProperty("user.home") + p.substring(1);
			b.setFile(Path.of(p));
		}
		b.setApplication(this);
		return b.build();
	});

	Supplier<IO.Consumer<HttpExchange>> handler = Lazy.of(() -> {
		var b = new ApplicationHandlerBuilder();
		b.setApplication(this);
		return b.build();
	});

	public Properties getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	public Persistence getPersistence() {
		try {
			return persistence.get();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public IO.Consumer<HttpExchange> getHandler() {
		return handler.get();
	}
}
