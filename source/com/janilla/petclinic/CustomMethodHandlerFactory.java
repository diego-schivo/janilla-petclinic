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

import java.util.Comparator;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import com.janilla.http.HttpExchange;
import com.janilla.web.HandleException;
import com.janilla.web.MethodHandlerFactory;
import com.janilla.web.RenderableFactory;
import com.janilla.web.WebHandlerFactory;

/**
 * @author Diego Schivo
 */
public class CustomMethodHandlerFactory extends MethodHandlerFactory {

	public Properties configuration;

	public CustomMethodHandlerFactory(Set<Class<?>> types, Function<Class<?>, Object> targetResolver,
			Comparator<Invocation> invocationComparator, RenderableFactory renderableFactory,
			WebHandlerFactory rootFactory) {
		super(types, targetResolver, invocationComparator, renderableFactory, rootFactory);
	}

	@Override
	protected boolean handle(Invocation invocation, HttpExchange exchange) {
		if (Boolean.parseBoolean(configuration.getProperty("petclinic.live-demo"))
				&& !exchange.getRequest().getMethod().equals("GET"))
			throw new HandleException(new MethodBlockedException());
		return super.handle(invocation, exchange);
	}
}
