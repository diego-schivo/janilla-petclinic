package com.janilla.petclinic;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.janilla.http.HttpExchange;
import com.janilla.web.Render;

public abstract class LayoutRenderer extends Render.Renderer {

	static Pattern pathPrefix = Pattern.compile("^/\\w*");

	@Override
	public String apply(Object value, HttpExchange exchange) {
		var tt = templates("layout.html");
		return interpolate(tt.get(null), Map.<String, Object>of("navItems", Layout.navItems.stream().map(x -> {
			var m1 = pathPrefix.matcher(x.href());
			var m2 = pathPrefix.matcher(exchange.getRequest().getPath());
			return interpolate(tt.get("nav-item"),
					merge(x, Map.of("className",
							"nav-link " + (m1.find() && m2.find() && m1.group().equals(m2.group()) ? "active" : ""),
							"iconClass", "fa fa-" + x.icon())));
		}).collect(Collectors.joining()), "content", renderContent(value, exchange)));
	}

	protected abstract String renderContent(Object value, HttpExchange exchange);
}