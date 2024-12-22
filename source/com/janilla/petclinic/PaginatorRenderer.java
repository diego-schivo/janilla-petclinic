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

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.janilla.http.HttpExchange;
import com.janilla.web.Renderer;

public class PaginatorRenderer extends Renderer<Paginator> {

	@Override
	public String apply(Paginator paginator, HttpExchange exchange) {
		var tt = templates("paginator.html");
		var pp = IntStream.range(0, paginator.length()).mapToObj(x -> {
			var i = new Item(x != paginator.index() ? paginator.uri(x + 1) : null, null, null, x + 1);
			return interpolate(tt.get(i.href != null ? "item-on" : "item-off"), i);
		}).collect(Collectors.joining());
		var aa = IntStream.range(0, 4).mapToObj(x -> {
			var h = switch (x) {
			case 0, 1 -> paginator.index() != 0;
			default -> paginator.index() != paginator.length() - 1;
			};
			var i = new Item(h ? paginator.uri(switch (x) {
			case 0 -> 1;
			case 1 -> paginator.index();
			case 2 -> paginator.index() + 2;
			default -> paginator.length();
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
