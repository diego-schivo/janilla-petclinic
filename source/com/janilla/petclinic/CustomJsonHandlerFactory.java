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

import java.util.Iterator;

import com.janilla.http.HttpExchange;
import com.janilla.ioc.DiFactory;
import com.janilla.java.Java;
import com.janilla.json.JsonToken;
import com.janilla.json.ReflectionJsonIterator;
import com.janilla.web.JsonHandlerFactory;

/**
 * @author Diego Schivo
 */
public class CustomJsonHandlerFactory extends JsonHandlerFactory {

	protected final DiFactory diFactory;

	public CustomJsonHandlerFactory(DiFactory diFactory) {
		this.diFactory = diFactory;
	}

	@Override
	protected Iterator<JsonToken<?>> buildJsonIterator(Object object, HttpExchange exchange) {
		return diFactory.create(ReflectionJsonIterator.class, Java.hashMap("object", object, "includeType", false));
	}
}
