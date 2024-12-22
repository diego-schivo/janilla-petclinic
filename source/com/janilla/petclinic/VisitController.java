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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.janilla.http.HttpExchange;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Reflection;
import com.janilla.web.Handle;
import com.janilla.web.Render;

/**
 * @author Diego Schivo
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
public class VisitController {

	public Persistence persistence;

	@Handle(method = "GET", path = "/owners/(\\d+)/pets/(\\d+)/visits/new")
	public Object initCreate(long owner, long pet) throws IOException {
		var o = persistence.crud(Owner.class).read(owner);
		var p = persistence.crud(Pet.class).read(pet);
		var t = persistence.crud(PetType.class).read(p.type());
		var v = new Visit(null, null, LocalDate.now(), null);
		var c = persistence.crud(Visit.class);
		var w = c.read(c.filter("pet", pet)).toList();
		return new Form(o, p, t, v, w, null);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/pets/(\\d+)/visits/new")
	public Object create(long owner, long pet, Visit visit) throws IOException {
		var v = new Visit(null, pet, visit.date(), visit.description());
		var errors = validate(v);
		if (!errors.isEmpty())
			return Form.of(v, errors, persistence);

		persistence.crud(Visit.class).create(v);
		return URI.create("/owners/" + owner);
	}

	protected Map<String, List<String>> validate(Visit visit) {
		var errors = new HashMap<String, List<String>>();
		if (visit.date() == null)
			errors.computeIfAbsent("date", k -> new ArrayList<>()).add("must not be blank");
		if (visit.description() == null || visit.description().isBlank())
			errors.computeIfAbsent("description", k -> new ArrayList<>()).add("must not be blank");
		return errors;
	}

	@Render(FormRenderer.class)
	public record Form(Owner owner, Pet pet, PetType petType, Visit visit, List<Visit> previousVisits,
			Map<String, List<String>> errors) {

		static Form of(Visit visit, Map<String, List<String>> errors, Persistence persistence) throws IOException {
			var p = persistence.crud(Pet.class).read(visit.pet());
			var o = persistence.crud(Owner.class).read(p.owner());
			var t = persistence.crud(PetType.class).read(p.type());
			var c = persistence.crud(Visit.class);
			var w = c.read(c.filter("pet", p.id())).toList();
			return new Form(o, p, t, visit, w, errors);
		}
	}

	public static class FormRenderer extends LayoutRenderer {

		static Map<String, String> labels = Map.of("date", "Date", "description", "Description");

		@Override
		protected String renderContent(Object value, HttpExchange exchange) {
			var tt = templates("createOrUpdateVisitForm.html");
			var v = (Form) value;
			var on = v.owner.firstName() + " " + v.owner.lastName();
			var ff = Reflection.properties2(Visit.class)
					.filter(x -> !x.getName().equals("id") && !x.getName().equals("pet")).map(x -> {
						var n = x.getName();
						var l = labels.get(n);
						var v2 = x.get(v.visit);
						var ee = v.errors != null ? v.errors.get(n) : null;
						return new InputField(l, n, v2, ee, n.equals("date") ? "date" : "text");
					}).collect(Collectors.toMap(x -> x.name(), x -> x));
			var vv = v.previousVisits.stream().map(x -> {
				return interpolate(tt.get("visit"), x);
			}).collect(Collectors.joining());
			return interpolate(tt.get(null), merge(v, Map.of("ownerName", on), ff, Map.of("visits", vv)));
		}
	}
}
