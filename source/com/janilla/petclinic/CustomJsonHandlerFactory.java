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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import com.janilla.http.ExchangeContext;
import com.janilla.json.JsonToken;
import com.janilla.json.ReflectionJsonIterator;
import com.janilla.persistence.Persistence;
import com.janilla.web.JsonHandlerFactory;

/**
 * @author Diego Schivo
 */
public class CustomJsonHandlerFactory extends JsonHandlerFactory {

	Properties configuration;

	Persistence persistence;

	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Override
	protected Iterator<JsonToken<?>> newJsonIterator(Object object, ExchangeContext context) {
		return new ReflectionJsonIterator(object) {

			@Override
			public Iterator<JsonToken<?>> newValueIterator(Object object) {
				var o = getStack().peek();
				if (o instanceof Entry e)
					switch ((String) e.getKey()) {
					case "specialties":
						if (object instanceof Collection<?> c) {
							var i = c.stream().mapToLong(x -> (Long) x).toArray();
							try {
								object = persistence.getCrud(Specialty.class).read(i).toList();
							} catch (IOException f) {
								throw new UncheckedIOException(f);
							}
						}
						break;
					}
				return super.newValueIterator(object);
			}
		};
	}
}
