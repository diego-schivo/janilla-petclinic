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

import com.janilla.frontend.RenderEngine.ObjectAndType;
import com.janilla.http.HttpExchange;
import com.janilla.web.Error;
import com.janilla.web.ExceptionHandlerFactory;
import com.janilla.web.HandlerFactory;
import com.janilla.web.Render;

/**
 * @author Diego Schivo
 */
public class CustomExceptionHandlerFactory extends ExceptionHandlerFactory {

	protected HandlerFactory mainFactory;

	public void setMainFactory(HandlerFactory mainFactory) {
		this.mainFactory = mainFactory;
	}

	@Override
	protected void handle(Error error, HttpExchange context) throws IOException {
		super.handle(error, context);
		var e = context.getException();
		if (e.getClass().isAnnotationPresent(Render.class))
			mainFactory.createHandler(new ObjectAndType(null, e, null), context).accept(context);
	}
}
