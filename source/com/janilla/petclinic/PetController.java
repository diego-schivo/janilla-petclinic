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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.janilla.persistence.Persistence;
import com.janilla.reflect.Reflection;
import com.janilla.web.Handle;
import com.janilla.web.Render;

/**
 * @author Diego Schivo
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
public class PetController {

	public Persistence persistence;

	@Handle(method = "GET", path = "/owners/(\\d+)/pets/new")
	public Object initCreate(long owner) {
		var p = new Pet(null, null, null, null, owner);
		return Form.of(p, null, persistence);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/pets/new")
	public Object create(long owner, Pet pet) {
		var p = new Pet(null, pet.name(), pet.birthDate(), pet.type(), owner);
		var ee = validate(p);
		if (!ee.isEmpty())
			return Form.of(p, ee, persistence);
		var p2 = persistence.crud(Pet.class).create(p);
		return URI.create("/owners/" + p2.owner());
	}

	@Handle(method = "GET", path = "/owners/(\\d+)/pets/(\\d+)/edit")
	public Object initUpdate(long owner, long id) {
		var p = persistence.crud(Pet.class).read(id);
		return Form.of(p, null, persistence);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/pets/(\\d+)/edit")
	public Object update(long owner, long id, Pet pet) {
		var p = new Pet(id, pet.name(), pet.birthDate(), pet.type(), owner);
		var ee = validate(p);
		if (!ee.isEmpty())
			return Form.of(p, ee, persistence);
		var p2 = persistence.crud(Pet.class).update(id,
				x -> Reflection.copy(p, x, y -> !Set.of("id", "owner").contains(y)));
		return URI.create("/owners/" + p2.owner());
	}

	protected Map<String, List<String>> validate(Pet pet) {
		var m = new LinkedHashMap<String, List<String>>();
		if (pet.name() == null || pet.name().isBlank())
			m.computeIfAbsent("name", _ -> new ArrayList<>()).add("must not be blank");
		if (pet.birthDate() == null)
			m.computeIfAbsent("birthDate", _ -> new ArrayList<>()).add("must not be blank");
		if (pet.type() == null)
			m.computeIfAbsent("type", _ -> new ArrayList<>()).add("must not be blank");
		return m;
	}

	@Render(template = "createOrUpdatePetForm.html")
	public record Form(Owner owner, Pet pet, List<PetType> types, Map<String, List<String>> errors) {

		private static final Map<String, String> LABELS = Map.of("name", "Name", "birthDate", "Birth Date", "type",
				"Type");

		public static Form of(Pet pet, Map<String, List<String>> errors, Persistence persistence) {
			var o = persistence.crud(Owner.class).read(pet.owner());
			var tc = persistence.crud(PetType.class);
			var tt = tc.read(tc.filter(null)).toList();
			return new Form(o, pet, tt, errors);
		}

		public String heading() {
			return pet.id() == null ? "New Pet" : "Pet";
		}

		public Function<String, FormField<?>> fields() {
			return x -> {
				var l = LABELS.get(x);
				var v = Reflection.property(Pet.class, x).get(pet);
				var ee = errors != null ? errors.get(x) : null;
				return switch (x) {
				case "type" -> {
					var ii = types.stream()
							.collect(Collectors.toMap(PetType::id, PetType::name, (y, _) -> y, LinkedHashMap::new));
					yield new SelectField<>(l, x, (Long) v, ee, ii);
				}
				case "birthDate" -> new InputField<>(l, x, (LocalDate) v, ee, "date");
				default -> new InputField<>(l, x, (String) v, ee, "text");
				};
			};
		}

		public String button() {
			return pet.id() == null ? "Add Pet" : "Update Pet";
		}
	}
}
