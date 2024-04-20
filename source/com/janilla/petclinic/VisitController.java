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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.janilla.persistence.Persistence;
import com.janilla.reflect.Reflection;
import com.janilla.web.Handle;
import com.janilla.web.Render;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Diego Schivo
 */
public class VisitController {

	private Persistence persistence;

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "/owners/(\\d+)/pets/(\\d+)/visits/new")
	public Object initCreate(long owner, long pet) throws IOException {
		var o = persistence.getCrud(Owner.class).read(owner);
		var p = persistence.getCrud(Pet.class).read(pet);
		var t = persistence.getCrud(PetType.class).read(p.getType());
		var v = new Visit();
		v.setDate(LocalDate.now());
		var c = persistence.getCrud(Visit.class);
		var w = c.read(c.filter("pet", pet)).toList();
		return new Form(o, p, t, v, w, null);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/pets/(\\d+)/visits/new")
	public Object create(long owner, long pet, Visit visit) throws IOException {
		visit.setPet(pet);
		var errors = validate(visit);
		if (!errors.isEmpty())
			return Form.of(visit, errors, persistence);

		persistence.getCrud(Visit.class).create(visit);
		return URI.create("/owners/" + owner);
	}

	protected Map<String, Collection<String>> validate(Visit visit) {
		var errors = new HashMap<String, Collection<String>>();
		if (visit.getDate() == null)
			errors.computeIfAbsent("date", k -> new ArrayList<>()).add("must not be blank");
		if (visit.getDescription() == null || visit.getDescription().isBlank())
			errors.computeIfAbsent("description", k -> new ArrayList<>()).add("must not be blank");
		return errors;
	}

	@Render(template = "createOrUpdateVisitForm.html")
	public record Form(Owner owner, Pet pet, PetType petType, Visit visit,
			List<@Render(template = "createOrUpdateVisitForm-previousVisit.html") Visit> previousVisits,
			Map<String, Collection<String>> errors) {

		static Form of(Visit visit, Map<String, Collection<String>> errors, Persistence persistence)
				throws IOException {
			var p = persistence.getCrud(Pet.class).read(visit.getPet());
			var o = persistence.getCrud(Owner.class).read(p.getOwner());
			var t = persistence.getCrud(PetType.class).read(p.getType());
			var c = persistence.getCrud(Visit.class);
			var w = c.read(c.filter("pet", p.getId())).toList();
			return new Form(o, p, t, visit, w, errors);
		}

		static Map<String, String> labels = Map.of("date", "Date", "description", "Description");

		public Function<String, FormField> fields() {
			return n -> {
				try {
					var l = labels.get(n);
					var t = n.equals("date") ? "date" : "text";
					var v = Reflection.property(Visit.class, n).get(visit);
					var e = errors != null ? errors.get(n) : null;
					return new InputField(l, n, t, v, e);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			};
		}
	}
}
