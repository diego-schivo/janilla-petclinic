/*
 * Copyright 2012-2025 the original author or authors.
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
import java.util.List;

import com.janilla.persistence.Persistence;
import com.janilla.web.Bind;
import com.janilla.web.Handle;
import com.janilla.web.HtmlRenderer;
import com.janilla.web.Render;

/**
 * @author Diego Schivo
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Handle(path = "/vets")
public class VetController {

	public Persistence persistence;

	@Handle(method = "GET", path = "/vets.html")
	public Object find(@Bind("page") Integer page) {
		var vc = persistence.crud(Vet.class);
		var sc = persistence.crud(Specialty.class);
		var i = page != null ? page - 1 : 0;
		var vv = vc.list(i * 5, 5);
		var l = (int) Math.ceilDiv(vv.total(), 5);
		var ss = sc.read(sc.list());
		var rr = vc.read(vv.ids()).stream().map(x -> {
			var ss2 = ss.stream().filter(y -> x.specialties().contains(y.id())).toList();
			return new FindOutcome.Result(x, ss2);
		}).toList();
		var p = new Paginator(i, l, URI.create("/vets.html"));
		return new FindOutcome(rr, p);
	}

	@Handle(method = "GET")
	public Object find() {
		var c = persistence.crud(Vet.class);
		var v = c.read(c.list());
		return v;
	}

	@Render(template = "vetList.html")
	public record FindOutcome(List<Result> results, Paginator paginator) {

		@Render(template = "result")
		public record Result(Vet vet,
				@Render(renderer = SpecialtiesRenderer.class) List<@Render(template = "specialty") Specialty> specialties) {
		}

		public static class SpecialtiesRenderer extends HtmlRenderer<List<Specialty>> {

			@Override
			public String apply(List<Specialty> value) {
				return !value.isEmpty() ? super.apply(value) : "none";
			}
		}
	}
}
