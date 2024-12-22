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
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.janilla.http.HttpExchange;
import com.janilla.persistence.Persistence;
import com.janilla.web.Bind;
import com.janilla.web.Handle;
import com.janilla.web.Render;

/**
 * @author Diego Schivo
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
public class VetController {

	public Persistence persistence;

	@Handle(method = "GET", path = "/vets.html")
	public Object find(@Bind("page") Integer page) throws IOException {
		try {
			var vc = persistence.crud(Vet.class);
			var sc = persistence.crud(Specialty.class);
			var i = page != null ? page - 1 : 0;
			var vv = vc.list(i * 5, 5);
			var l = (int) ((vv.total() + 4) / 5);
			var ss = sc.read(sc.list()).toList();
			var rr = vc.read(vv.ids()).map(x -> {
				var ss2 = ss.stream().filter(y -> x.specialties().contains(y.id())).toList();
				return new FindOutcome.Result(x, ss2);
			}).toList();
			var p = new Paginator(i, l, URI.create("/vets.html"));
			return new FindOutcome(rr, p);
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	@Handle(method = "GET", path = "/vets")
	public Object find() throws IOException {
		var c = persistence.crud(Vet.class);
		var v = c.read(c.list()).toList();
		return v;
	}

	@Render(FindOutcomeRenderer.class)
	public record FindOutcome(List<Result> results, Paginator paginator) {

		public record Result(Vet vet, List<Specialty> specialties) {
		}
	}

	public static class FindOutcomeRenderer extends LayoutRenderer {

		@Override
		protected String renderContent(Object value, HttpExchange exchange) {
			var tt = templates("vetList.html");
			var v = (FindOutcome) value;
			var rr = v.results.stream().map(x -> {
				var n = x.vet.firstName() + " " + x.vet.lastName();
				var ss = x.specialties.stream().map(y -> {
					return interpolate(tt.get("specialty"), y);
				}).collect(Collectors.joining());
				if (ss.isEmpty())
					ss = interpolate(tt.get("specialty"), Map.of("name", "none"));
				return interpolate(tt.get("result"), merge(x, Map.of("name", n, "specialties", ss)));
			}).collect(Collectors.joining());
			return interpolate(tt.get(null), merge(v, Map.of("results", rr)));
		}
	}
}
