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
//package com.janilla.petclinic;
//
//import com.janilla.frontend.RenderEngine;
//import com.janilla.http.HttpExchange;
//import com.janilla.web.TemplateHandlerFactory;
//
///**
// * @author Diego Schivo
// */
//public class CustomTemplateHandlerFactory extends TemplateHandlerFactory {
//
//	@Override
//	protected void render(RenderEngine.Entry input, HttpExchange exchange) {
//		var ex = (CustomHttpExchange) exchange;
//		var a = ex.getRequest().getHeaderValue("accept");
//		if (ex.layout == null && !a.equals("*/*")) {
//			ex.layout = new Layout(ex.getRequest().getPath(), input);
//			input = RenderEngine.Entry.of(null, ex.layout, null);
//		}
//		super.render(input, exchange);
//	}
//}
