/*
 * Copyright 2012-2025 the original author or authors.
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

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import com.janilla.http.HttpHandler;
import com.janilla.http.HttpServer;
import com.janilla.json.MapAndType;
import com.janilla.net.Net;
import com.janilla.persistence.ApplicationPersistenceBuilder;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Factory;
import com.janilla.util.Util;
import com.janilla.web.ApplicationHandlerBuilder;
import com.janilla.web.RenderableFactory;

/**
 * @author Diego Schivo
 * @author Dave Syer
 */
public class PetClinicApplication {

	public static void main(String[] args) {
		try {
			var pp = new Properties();
			try (var s1 = PetClinicApplication.class.getResourceAsStream("configuration.properties")) {
				pp.load(s1);
				if (args.length > 0) {
					var p = args[0];
					if (p.startsWith("~"))
						p = System.getProperty("user.home") + p.substring(1);
					try (var s2 = Files.newInputStream(Path.of(p))) {
						pp.load(s2);
					}
				}
			}
			var pca = new PetClinicApplication(pp);
			HttpServer s;
			{
				SSLContext sc;
				try (var is = Net.class.getResourceAsStream("testkeys")) {
					sc = Net.getSSLContext("JKS", is, "passphrase".toCharArray());
				}
				s = pca.factory.create(HttpServer.class, Map.of("sslContext", sc, "handler", pca.handler));
			}
			var p = Integer.parseInt(pca.configuration.getProperty("petclinic.server.port"));
			s.serve(new InetSocketAddress(p));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public Properties configuration;

	public Factory factory;

	public Persistence persistence;

	public RenderableFactory renderableFactory;

	public HttpHandler handler;

	public MapAndType.TypeResolver typeResolver;

	public Iterable<Class<?>> types;

	public PetClinicApplication(Properties configuration) {
		this.configuration = configuration;
		types = Util.getPackageClasses(getClass().getPackageName()).toList();
		factory = new Factory(types, this);
		typeResolver = factory.create(MapAndType.DollarTypeResolver.class);
		{
			var p = configuration.getProperty("petclinic.database.file");
			if (p.startsWith("~"))
				p = System.getProperty("user.home") + p.substring(1);
			var pb = factory.create(ApplicationPersistenceBuilder.class, Map.of("databaseFile", Path.of(p)));
			persistence = pb.build();
		}
		renderableFactory = new RenderableFactory();
		handler = factory.create(ApplicationHandlerBuilder.class).build();
	}

	public PetClinicApplication application() {
		return this;
	}
}
