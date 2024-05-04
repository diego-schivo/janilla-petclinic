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

import java.lang.reflect.AnnotatedType;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import com.janilla.frontend.RenderEngine;
import com.janilla.frontend.Renderer;
import com.janilla.web.Render;

@Render("layout.html")
public record Layout(URI uri, RenderEngine.Entry entry) implements Renderer {

	protected static List<NavItem> navItems = List.of(new NavItem("home", "Home", URI.create("/"), "home page"),
			new NavItem("search", "Find owners", URI.create("/owners/find"), "find owners"),
			new NavItem("list", "Veterinarians", URI.create("/vets.html"), "veterinarians"),
			new NavItem("exclamation-triangle", "Error", URI.create("/oups"),
					"trigger a RuntimeException to see how it is handled"));

	public List<NavItem> navItems() {
		return navItems;
	}

	private static Pattern pathPrefix = Pattern.compile("^/\\w*");

	@Override
	public boolean evaluate(RenderEngine engine) {
		record A(Layout layout, Object content) {
		}
		record B(NavItem navItem, Object activeClass) {
		}
		return engine.match(A.class, (i, o) -> {
			o.setValue(entry.getValue());
			var r = entry.getType() instanceof AnnotatedType x ? x.getAnnotation(Render.class) : null;
			if (r != null)
				o.setTemplate(!r.template().isEmpty() ? r.template() : r.value());
		}) || engine.match(B.class, (i, o) -> {
			var m1 = pathPrefix.matcher(i.navItem.href.getPath());
			var m2 = pathPrefix.matcher(uri.getPath());
			if (m1.find() && m2.find() && m1.group().equals(m2.group()))
				o.setValue("active");
		});
	}

	@Render("layout-navItem.html")
	public record NavItem(String icon, String text, URI href, String title) {
	}
}
