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
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.janilla.http.HttpHandler;
import com.janilla.http.HttpProtocol;
import com.janilla.net.Net;
import com.janilla.net.Server;
import com.janilla.persistence.ApplicationPersistenceBuilder;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Factory;
import com.janilla.util.Util;
import com.janilla.web.ApplicationHandlerBuilder;

/**
 * @author Diego Schivo
 * @author Dave Syer
 */
public class PetClinicApplication {

	public static void main(String[] args) {
		try {
			var pp = new Properties();
			try (var is = PetClinicApplication.class.getResourceAsStream("configuration.properties")) {
				pp.load(is);
				if (args.length > 0) {
					var p = args[0];
					if (p.startsWith("~"))
						p = System.getProperty("user.home") + p.substring(1);
					pp.load(Files.newInputStream(Path.of(p)));
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			var a = new PetClinicApplication(pp);
			var hp = a.factory.create(HttpProtocol.class);
			try (var is = Net.class.getResourceAsStream("testkeys")) {
				hp.setSslContext(Net.getSSLContext("JKS", is, "passphrase".toCharArray()));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			hp.setHandler(a.handler);
			var s = new Server();
			s.setAddress(new InetSocketAddress(Integer.parseInt(a.configuration.getProperty("petclinic.server.port"))));
			s.setProtocol(hp);
			s.serve();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public Properties configuration;

	public Factory factory;

	public HttpHandler handler;

	public Persistence persistence;

	public PetClinicApplication(Properties configuration) {
		this.configuration = configuration;
		factory = new Factory();
		factory.setTypes(Util.getPackageClasses(getClass().getPackageName()).toList());
		factory.setSource(this);
		handler = factory.create(ApplicationHandlerBuilder.class).build();
		{
			var pb = factory.create(ApplicationPersistenceBuilder.class);
			var p = configuration.getProperty("petclinic.database.file");
			if (p.startsWith("~"))
				p = System.getProperty("user.home") + p.substring(1);
			pb.setFile(Path.of(p));
			persistence = pb.build();
		}
	}

	public PetClinicApplication application() {
		return this;
	}
}
