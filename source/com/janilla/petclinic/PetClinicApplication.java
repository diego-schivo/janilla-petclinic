/*
 * Copyright 2012-2026 the original author or authors.
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

import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import com.janilla.http.HttpHandler;
import com.janilla.http.HttpServer;
import com.janilla.ioc.DiFactory;
import com.janilla.java.Java;
import com.janilla.net.Net;
import com.janilla.persistence.ApplicationPersistenceBuilder;
import com.janilla.persistence.Persistence;
import com.janilla.web.ApplicationHandlerFactory;
import com.janilla.web.Invocable;
import com.janilla.web.NotFoundException;
import com.janilla.web.RenderableFactory;

/**
 * @author Diego Schivo
 * @author Dave Syer
 */
public class PetClinicApplication {

	public static final AtomicReference<PetClinicApplication> INSTANCE = new AtomicReference<>();

	public static void main(String[] args) {
		try {
			PetClinicApplication a;
			{
				var f = new DiFactory(Stream.of(PetClinicApplication.class.getPackageName(), "com.janilla.web")
						.flatMap(x -> Java.getPackageClasses(x).stream()).toList(), INSTANCE::get);
				a = f.create(PetClinicApplication.class,
						Java.hashMap("diFactory", f, "configurationFile",
								args.length > 0 ? Path.of(
										args[0].startsWith("~") ? System.getProperty("user.home") + args[0].substring(1)
												: args[0])
										: null));
			}

			HttpServer s;
			{
				SSLContext c;
				try (var x = Net.class.getResourceAsStream("localhost")) {
					c = Net.getSSLContext(Map.entry("JKS", x), "passphrase".toCharArray());
				}
				var p = Integer.parseInt(a.configuration.getProperty("petclinic.server.port"));
				s = a.diFactory.create(HttpServer.class,
						Map.of("sslContext", c, "endpoint", new InetSocketAddress(p), "handler", a.handler));
			}
			s.serve();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected final Properties configuration;

	protected final DiFactory diFactory;

	protected final List<Path> files;

	protected final HttpHandler handler;

	protected final List<Invocable> invocables;

	protected final Persistence persistence;

	protected final RenderableFactory renderableFactory;

	public PetClinicApplication(DiFactory diFactory, Path configurationFile) {
		this.diFactory = diFactory;
		if (!INSTANCE.compareAndSet(null, this))
			throw new IllegalStateException();
		configuration = diFactory.create(Properties.class, Collections.singletonMap("file", configurationFile));
		{
			var p = configuration.getProperty("petclinic.database.file");
			if (p.startsWith("~"))
				p = System.getProperty("user.home") + p.substring(1);
			var pb = diFactory.create(ApplicationPersistenceBuilder.class, Map.of("databaseFile", Path.of(p)));
			persistence = pb.build();
		}

		invocables = types().stream()
				.flatMap(x -> Arrays.stream(x.getMethods())
						.filter(y -> !Modifier.isStatic(y.getModifiers()) && !y.isBridge())
						.map(y -> new Invocable(x, y)))
				.toList();
		files = Stream.of("com.janilla.frontend", PetClinicApplication.class.getPackageName())
				.flatMap(x -> Java.getPackagePaths(x).stream().filter(Files::isRegularFile)).toList();
		renderableFactory = diFactory.create(RenderableFactory.class);
		{
			var f = diFactory.create(ApplicationHandlerFactory.class);
			handler = x -> {
				var h = f.createHandler(Objects.requireNonNullElse(x.exception(), x.request()));
				if (h == null)
					throw new NotFoundException(x.request().getMethod() + " " + x.request().getTarget());
				return h.handle(x);
			};
		}
	}

	public PetClinicApplication application() {
		return this;
	}

	public Properties configuration() {
		return configuration;
	}

	public DiFactory diFactory() {
		return diFactory;
	}

	public List<Path> files() {
		return files;
	}

	public HttpHandler handler() {
		return handler;
	}

	public List<Invocable> invocables() {
		return invocables;
	}

	public Persistence persistence() {
		return persistence;
	}

	public RenderableFactory renderableFactory() {
		return renderableFactory;
	}

	public Collection<Class<?>> types() {
		return diFactory.types();
	}
}
