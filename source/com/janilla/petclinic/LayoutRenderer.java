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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.janilla.http.HttpExchange;
import com.janilla.web.Renderer;

public abstract class LayoutRenderer<T> extends Renderer<T> {

	protected static List<NavItem> navItems = List.of(new NavItem("home", "Home", "/", "home page"),
			new NavItem("search", "Find owners", "/owners/find", "find owners"),
			new NavItem("list", "Veterinarians", "/vets.html", "veterinarians"), new NavItem("exclamation-triangle",
					"Error", "/oups", "trigger a RuntimeException to see how it is handled"));

	protected static Pattern pathPrefix = Pattern.compile("^/\\w*");

	@Override
	public String apply(T value, HttpExchange exchange) {
		var tt = templates("layout.html");
		return interpolate(tt.get(null), Map.<String, Object>of("navItems", navItems.stream().map(x -> {
			var m1 = pathPrefix.matcher(x.href());
			var m2 = pathPrefix.matcher(exchange.getRequest().getPath());
			return interpolate(tt.get("nav-item"),
					merge(x, Map.of("className",
							"nav-link " + (m1.find() && m2.find() && m1.group().equals(m2.group()) ? "active" : ""),
							"iconClass", "fa fa-" + x.icon())));
		}).collect(Collectors.joining()), "content", renderContent(value, exchange)));
	}

	protected abstract String renderContent(T value, HttpExchange exchange);

	public record NavItem(String icon, String text, String href, String title) {
	}
}
