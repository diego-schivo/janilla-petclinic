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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.janilla.persistence.Persistence;
import com.janilla.reflect.Reflection;
import com.janilla.web.Handle;
import com.janilla.web.Render;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Diego Schivo
 */
public class PetController {

	private Persistence persistence;

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "/owners/(\\d+)/pets/new")
	public Object initCreate(long owner) throws IOException {
		var p = new Pet(null, null, null, null, owner);
		return Form.of(p, null, persistence);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/pets/new")
	public Object create(long owner, Pet pet) throws IOException {
		var p = new Pet(null, pet.name(), pet.birthDate(), pet.type(), owner);
		var errors = validate(p);
		if (!errors.isEmpty())
			return Form.of(p, errors, persistence);

		persistence.getCrud(Pet.class).create(p);
		return URI.create("/owners/" + owner);
	}

	@Handle(method = "GET", path = "/owners/(\\d+)/pets/(\\d+)/edit")
	public Object initUpdate(long owner, long id) throws IOException {
		var p = persistence.getCrud(Pet.class).read(id);
		return Form.of(p, null, persistence);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/pets/(\\d+)/edit")
	public Object update(long owner, long id, Pet pet) throws IOException {
		var p = new Pet(id, pet.name(), pet.birthDate(), pet.type(), owner);
		var errors = validate(p);
		if (!errors.isEmpty())
			return Form.of(p, errors, persistence);

		var q = persistence.getCrud(Pet.class).update(id,
				x -> Reflection.copy(p, x, y -> !Set.of("id", "owner").contains(y)));
		return URI.create("/owners/" + q.owner());
	}

	protected Map<String, Collection<String>> validate(Pet pet) {
		var errors = new HashMap<String, Collection<String>>();
		if (pet.name() == null || pet.name().isBlank())
			errors.computeIfAbsent("name", k -> new ArrayList<>()).add("must not be blank");
		if (pet.birthDate() == null)
			errors.computeIfAbsent("birthDate", k -> new ArrayList<>()).add("must not be blank");
		if (pet.type() == null)
			errors.computeIfAbsent("type", k -> new ArrayList<>()).add("must not be blank");
		return errors;
	}

	@Render("createOrUpdatePetForm.html")
	public record Form(Owner owner, Pet pet, Collection<PetType> types, Map<String, Collection<String>> errors) {

		static Form of(Pet pet, Map<String, Collection<String>> errors, Persistence persistence) throws IOException {
			var o = persistence.getCrud(Owner.class).read(pet.owner());
			var c = persistence.getCrud(PetType.class);
			var t = c.read(c.filter(null)).toList();
			return new Form(o, pet, t, errors);
		}

		public String heading() {
			return (pet.id() == null ? "New " : "") + "Pet";
		}

		static Map<String, String> labels = Map.of("name", "Name", "birthDate", "Birth Date", "type", "Type");

		public Function<String, FormField> fields() {
			return n -> {
				var l = labels.get(n);
				var v = Reflection.property(Pet.class, n).get(pet);
				var e = errors != null ? errors.get(n) : null;
				return switch (n) {
				case "birthDate" -> new InputField(l, n, "date", v, e);
				case "type" -> {
					var i = types.stream().collect(
							Collectors.toMap(PetType::id, PetType::name, (a, b) -> a, LinkedHashMap::new));
					yield new SelectField(l, n, i, v, e);
				}
				default -> new InputField(l, n, "text", v, e);
				};
			};
		}

		public String button() {
			return (pet == null || pet.id() == null ? "Add" : "Update") + " Pet";
		}
	}
}
