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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.janilla.json.JsonToken;
import com.janilla.json.ReflectionJsonIterator;
import com.janilla.persistence.Persistence;

/**
 * @author Diego Schivo
 */
public class CustomReflectionJsonIterator extends ReflectionJsonIterator {

	protected final Persistence persistence;

	public CustomReflectionJsonIterator(Object object, boolean includeType, Persistence persistence) {
		super(object, includeType);
		this.persistence = persistence;
	}

	@Override
	public Iterator<JsonToken<?>> newValueIterator(Object object) {
		var o = stack().peek();
		if (o instanceof Map.Entry e)
			switch ((String) e.getKey()) {
			case "specialties":
				if (object instanceof List<?> c) {
					var ll = c.stream().map(x -> (Long) x).toList();
					object = persistence.crud(Specialty.class).read(ll);
				}
				break;
			}
		return super.newValueIterator(object);
	}
}
