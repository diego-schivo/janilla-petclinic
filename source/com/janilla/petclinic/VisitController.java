/*
 * Copyright 2012-2026 the original author or authors.
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.janilla.backend.persistence.Persistence;
import com.janilla.java.Reflection;
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
@Handle(path = "/owners/(\\d+)/pets/(\\d+)/visits")
public class VisitController {

	protected final Persistence persistence;

	public VisitController(Persistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "new")
	public Object initCreate(long owner, long pet) {
		var o = persistence.crud(Owner.class).read(owner);
		var p = persistence.crud(Pet.class).read(pet);
		var t = persistence.crud(PetType.class).read(p.type());
		var v = new Visit(null, null, LocalDate.now(), null);
		var c = persistence.crud(Visit.class);
		var w = c.read(c.filter("pet", new Object[] { pet }));
		return new Form(o, p, t, v, w, null);
	}

	@Handle(method = "POST", path = "new")
	public Object create(long owner, long pet, Visit visit) {
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
			errors.computeIfAbsent("date", _ -> new ArrayList<>()).add("must not be blank");
		if (visit.description() == null || visit.description().isBlank())
			errors.computeIfAbsent("description", _ -> new ArrayList<>()).add("must not be blank");
		return errors;
	}

	@Render(template = "createOrUpdateVisitForm.html")
	public record Form(Owner owner, Pet pet, PetType petType, Visit visit,
			List<@Render(template = "visit") Visit> previousVisits, Map<String, List<String>> errors) {

		private static final Map<String, String> LABELS = Map.of("date", "Date", "description", "Description");

		public static Form of(Visit visit, Map<String, List<String>> errors, Persistence persistence) {
			var p = persistence.crud(Pet.class).read(visit.pet());
			var o = persistence.crud(Owner.class).read(p.owner());
			var t = persistence.crud(PetType.class).read(p.type());
			var c = persistence.crud(Visit.class);
			var w = c.read(c.filter("pet", new Object[] { p.id() }));
			return new Form(o, p, t, visit, w, errors);
		}

		public Function<String, FormField<?>> fields() {
			return x -> {
				var l = LABELS.get(x);
				var v = Reflection.property(Visit.class, x).get(visit);
				var ee = errors != null ? errors.get(x) : null;
				return switch (x) {
				case "date" -> new InputField<>(l, x, (LocalDate) v, ee, "date");
				default -> new InputField<>(l, x, (String) v, ee, "text");
				};
			};
		}
	}
}
