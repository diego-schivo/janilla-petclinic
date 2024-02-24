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
import java.util.Properties;

import com.janilla.http.HttpExchange;
import com.janilla.web.HandleException;
import com.janilla.web.MethodHandlerFactory;
import com.janilla.web.MethodInvocation;

/**
 * @author Diego Schivo
 */
public class CustomMethodHandlerFactory extends MethodHandlerFactory {

	Properties configuration;

	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void handle(MethodInvocation invocation, HttpExchange exchange) throws IOException {
		if (Boolean.parseBoolean(configuration.getProperty("petclinic.disable-unsafe-actions")))
			switch (exchange.getRequest().getMethod().name()) {
			case "GET":
				break;
			default:
				throw new HandleException(new MethodBlockedException());
			}
		super.handle(invocation, exchange);
	}
}
