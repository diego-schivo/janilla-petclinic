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
import java.net.URI;
import java.util.List;
import java.util.Set;

import com.janilla.frontend.RenderEngine.ObjectAndType;
import com.janilla.http.HttpExchange;
import com.janilla.petclinic.Layout.NavItem;
import com.janilla.web.TemplateHandlerFactory;

/**
 * @author Diego Schivo
 */
public class CustomTemplateHandlerFactory extends TemplateHandlerFactory {

	static ThreadLocal<Layout> layout = new ThreadLocal<>();

	@Override
	protected void render(ObjectAndType input, HttpExchange context) throws IOException {
		var l = layout.get();
		var r = false;
		if (l == null) {
			l = toLayout(input.object());
			if (l != null) {
				input = new ObjectAndType(l, null);
				r = true;
			}
		}
		try {
			super.render(input, context);
		} finally {
			if (r)
				layout.remove();
		}
	}

	static Layout toLayout(Object object) {
		var o = object;
		var c = o.getClass();
		var d = c.getEnclosingClass();
		var a1 = c == WelcomeController.class;
		var a2 = d != null && Set.of(OwnerController.class, PetController.class, VisitController.class).contains(d);
		var a3 = d == VetController.class;
		var a4 = c == ShowcaseException.class;
		if (a1 || a2 || a3 || a4) {
			var i1 = new NavItem("home", "Home", URI.create("/"), "home page", a1);
			var i2 = new NavItem("search", "Find owners", URI.create("/owners/find"), "find owners", a2);
			var i3 = new NavItem("list", "Veterinarians", URI.create("/vets.html"), "veterinarians", a3);
			var i4 = new NavItem("exclamation-triangle", "Error", URI.create("/oups"),
					"trigger a RuntimeException to see how it is handled", a4);
			return new Layout(List.of(i1, i2, i3, i4), o);
		}
		return null;
	}
}
