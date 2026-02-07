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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import com.janilla.backend.persistence.PersistenceBuilder;
import com.janilla.backend.persistence.Persistence;
import com.janilla.backend.persistence.Store;
import com.janilla.http.HttpHandler;
import com.janilla.http.HttpServer;
import com.janilla.ioc.DiFactory;
import com.janilla.java.DollarTypeResolver;
import com.janilla.java.Java;
import com.janilla.java.TypeResolver;
import com.janilla.web.ApplicationHandlerFactory;
import com.janilla.web.Invocable;
import com.janilla.web.InvocationResolver;
import com.janilla.web.NotFoundException;
import com.janilla.web.RenderableFactory;
import com.janilla.web.ResourceMap;

/**
 * @author Diego Schivo
 * @author Dave Syer
 */
public class PetClinicApplication {

	public static void main(String[] args) {
		IO.println(ProcessHandle.current().pid());
		var f = new DiFactory(Stream.of("com.janilla.web", PetClinicApplication.class.getPackageName())
				.flatMap(x -> Java.getPackageClasses(x, false).stream()).toList());
		serve(f, args.length > 0 ? args[0] : null);
	}

	protected static void serve(DiFactory diFactory, String configurationPath) {
		PetClinicApplication a;
		{
			a = diFactory.create(PetClinicApplication.class,
					Java.hashMap("diFactory", diFactory, "configurationFile",
							configurationPath != null ? Path.of(configurationPath.startsWith("~")
									? System.getProperty("user.home") + configurationPath.substring(1)
									: configurationPath) : null));
		}

		SSLContext c;
		{
			var p = a.configuration.getProperty("petclinic.server.keystore.path");
			var w = a.configuration.getProperty("petclinic.server.keystore.password");
			if (p.startsWith("~"))
				p = System.getProperty("user.home") + p.substring(1);
			var f = Path.of(p);
			if (!Files.exists(f))
				Java.generateKeyPair(f, w);
			try (var s = Files.newInputStream(f)) {
				c = Java.sslContext(s, w.toCharArray());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		HttpServer s;
		{
			var p = Integer.parseInt(a.configuration.getProperty("petclinic.server.port"));
			s = a.diFactory.create(HttpServer.class,
					Map.of("sslContext", c, "endpoint", new InetSocketAddress(p), "handler", a.handler));
		}
		s.serve();
	}

	protected final Properties configuration;

	protected final DiFactory diFactory;

	protected final HttpHandler handler;

	protected final InvocationResolver invocationResolver;

	protected final Persistence persistence;

	protected final RenderableFactory renderableFactory;

	protected final List<Class<?>> resolvables;

	protected final ResourceMap resourceMap;

	protected final List<Class<?>> storables;

	protected final TypeResolver typeResolver;

	public PetClinicApplication(DiFactory diFactory, Path configurationFile) {
		this.diFactory = diFactory;
		diFactory.context(this);
		configuration = diFactory.create(Properties.class, Collections.singletonMap("file", configurationFile));

		{
			Map<String, Class<?>> m = diFactory.types().stream()
					.collect(Collectors.toMap(x -> x.getSimpleName(), x -> x, (_, x) -> x, LinkedHashMap::new));
			resolvables = m.values().stream().toList();
		}
		typeResolver = diFactory.create(DollarTypeResolver.class);

		storables = resolvables.stream().filter(x -> x.isAnnotationPresent(Store.class)).toList();
		{
			var f = configuration.getProperty("petclinic.database.file");
			if (f.startsWith("~"))
				f = System.getProperty("user.home") + f.substring(1);
			var b = diFactory.create(PersistenceBuilder.class, Map.of("databaseFile", Path.of(f)));
			persistence = b.build(diFactory);
		}

		invocationResolver = diFactory.create(InvocationResolver.class,
				Map.of("invocables",
						diFactory.types().stream()
								.flatMap(x -> Arrays.stream(x.getMethods())
										.filter(y -> !Modifier.isStatic(y.getModifiers()) && !y.isBridge())
										.map(y -> new Invocable(x, y)))
								.toList(),
						"instanceResolver", (Function<Class<?>, Object>) x -> {
							var y = diFactory.context();
//							IO.println("x=" + x + ", y=" + y);
							return x.isAssignableFrom(y.getClass()) ? diFactory.context() : diFactory.create(x);
						}));
		resourceMap = diFactory.create(ResourceMap.class,
				Map.of("paths",
						Map.of("", Stream.of("com.janilla.frontend", PetClinicApplication.class.getPackageName())
								.flatMap(x -> Java.getPackagePaths(x, false).filter(Files::isRegularFile)).toList())));
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

	public HttpHandler handler() {
		return handler;
	}

	public InvocationResolver invocationResolver() {
		return invocationResolver;
	}

	public Persistence persistence() {
		return persistence;
	}

	public RenderableFactory renderableFactory() {
		return renderableFactory;
	}

	public List<Class<?>> resolvables() {
		return resolvables;
	}

	public ResourceMap resourceMap() {
		return resourceMap;
	}

	public List<Class<?>> storables() {
		return storables;
	}

	public TypeResolver typeResolver() {
		return typeResolver;
	}
}
