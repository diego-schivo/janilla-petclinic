package com.janilla.petclinic;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.janilla.http.HttpExchange;
import com.janilla.web.Render;

public class PaginatorRenderer extends Render.Renderer {

	@Override
	public String apply(Object value, HttpExchange exchange) {
		var tt = templates("paginator.html");
		var p = (Paginator) value;
		var pp = IntStream.range(0, p.length()).mapToObj(x -> {
			var i = new Item(x != p.index() ? p.uri(x + 1) : null, null, null, x + 1);
			return interpolate(tt.get(i.href != null ? "item-on" : "item-off"), i);
		}).collect(Collectors.joining());
		var aa = IntStream.range(0, 4).mapToObj(x -> {
			var h = switch (x) {
			case 0, 1 -> p.index() != 0;
			default -> p.index() != p.length() - 1;
			};
			var i = new Item(h ? p.uri(switch (x) {
			case 0 -> 1;
			case 1 -> p.index();
			case 2 -> p.index() + 2;
			default -> p.length();
			}) : null, switch (x) {
			case 0 -> "First";
			case 1 -> "Previous";
			case 2 -> "Next";
			default -> "Last";
			}, "fa " + switch (x) {
			case 0 -> "fa-fast-backward";
			case 1 -> "fa-step-backward";
			case 2 -> "fa-step-forward";
			default -> "fa-fast-forward";
			}, null);
			return interpolate(tt.get(i.href != null ? "item-on" : "item-off"), i);
		}).collect(Collectors.joining());
		return interpolate(tt.get(null), Map.of("pages", pp, "arrows", aa));
	}

	public record Item(URI href, String title, String className, Object text) {
	}
}
