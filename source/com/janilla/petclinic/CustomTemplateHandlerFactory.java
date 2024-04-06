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

import com.janilla.frontend.RenderEngine;
import com.janilla.http.HttpExchange;
import com.janilla.web.TemplateHandlerFactory;

/**
 * @author Diego Schivo
 */
public class CustomTemplateHandlerFactory extends TemplateHandlerFactory {

	static ThreadLocal<Layout> layout = new ThreadLocal<>();

	@Override
	protected void render(RenderEngine.Entry input, HttpExchange exchange) throws IOException {
		var l = layout.get();
		var r = false;
		if (l == null) {
			l = new Layout(exchange.getRequest().getURI(), input);
			if (l != null) {
				input = new RenderEngine.Entry(null, l, null);
				r = true;
			}
		}
		try {
			super.render(input, exchange);
		} finally {
			if (r)
				layout.remove();
		}
	}
}
