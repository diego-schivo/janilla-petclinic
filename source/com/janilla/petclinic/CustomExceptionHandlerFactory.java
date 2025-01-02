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

import com.janilla.http.HttpExchange;
import com.janilla.web.Error;
import com.janilla.web.ExceptionHandlerFactory;
import com.janilla.web.RenderableFactory;
import com.janilla.web.WebHandlerFactory;

/**
 * @author Diego Schivo
 */
public class CustomExceptionHandlerFactory extends ExceptionHandlerFactory {

	public WebHandlerFactory mainFactory;

	public RenderableFactory renderableFactory;

	@Override
	protected boolean handle(Error error, HttpExchange exchange) {
		super.handle(error, exchange);
		var r = renderableFactory.createRenderable(null, exchange.getException());
		var h = mainFactory.createHandler(r, exchange);
		h.handle(exchange);
		return true;
	}
}
