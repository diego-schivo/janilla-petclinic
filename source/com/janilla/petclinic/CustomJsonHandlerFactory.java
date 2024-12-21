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

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.janilla.http.HttpExchange;
import com.janilla.json.JsonToken;
import com.janilla.json.ReflectionJsonIterator;
import com.janilla.persistence.Persistence;
import com.janilla.web.JsonHandlerFactory;

/**
 * @author Diego Schivo
 */
public class CustomJsonHandlerFactory extends JsonHandlerFactory {

	public Properties configuration;

	public Persistence persistence;

	@Override
	protected Iterator<JsonToken<?>> buildJsonIterator(Object object, HttpExchange exchange) {
		var i = new ReflectionJsonIterator() {

			@Override
			public Iterator<JsonToken<?>> buildValueIterator(Object object) {
				var o = getStack().peek();
				if (o instanceof Map.Entry e)
					switch ((String) e.getKey()) {
					case "specialties":
						if (object instanceof List<?> c) {
							var ll = c.stream().mapToLong(x -> (long) x).toArray();
							object = persistence.crud(Specialty.class).read(ll).toList();
						}
						break;
					}
				return super.buildValueIterator(object);
			}
		};
		i.setObject(object);
		return i;
	}
}
