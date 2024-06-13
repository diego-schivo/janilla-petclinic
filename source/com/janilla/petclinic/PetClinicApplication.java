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

import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Supplier;

import com.janilla.http.HttpServer;
import com.janilla.persistence.ApplicationPersistenceBuilder;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Factory;
import com.janilla.util.Lazy;
import com.janilla.util.Util;
import com.janilla.web.ApplicationHandlerBuilder;

/**
 * @author Diego Schivo
 * @author Dave Syer
 */
public class PetClinicApplication {

	public static void main(String[] args) throws Exception {
		var a = new PetClinicApplication();
		{
			var c = new Properties();
			try (var s = a.getClass().getResourceAsStream("configuration.properties")) {
				c.load(s);
			}
			a.configuration = c;
		}
		a.getPersistence();

		var s = a.getFactory().create(HttpServer.class);
		s.setPort(Integer.parseInt(a.configuration.getProperty("petclinic.server.port")));
		s.setHandler(a.getHandler());
		s.run();
	}

	public Properties configuration;

	private Supplier<Factory> factory = Lazy.of(() -> {
		var f = new Factory();
		f.setTypes(Util.getPackageClasses(getClass().getPackageName()).toList());
		f.setSource(this);
		return f;
	});

	private Supplier<Persistence> persistence = Lazy.of(() -> {
		var b = getFactory().create(ApplicationPersistenceBuilder.class);
		{
			var p = configuration.getProperty("petclinic.database.file");
			if (p.startsWith("~"))
				p = System.getProperty("user.home") + p.substring(1);
			b.setFile(Path.of(p));
		}
		return b.build();
	});

	Supplier<HttpServer.Handler> handler = Lazy.of(() -> {
		var b = getFactory().create(ApplicationHandlerBuilder.class);
		return b.build();
	});

	public PetClinicApplication getApplication() {
		return this;
	}

	public Factory getFactory() {
		return factory.get();
	}

	public Persistence getPersistence() {
		return persistence.get();
	}

	public HttpServer.Handler getHandler() {
		return handler.get();
	}
}
