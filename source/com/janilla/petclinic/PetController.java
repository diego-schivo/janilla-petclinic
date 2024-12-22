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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 */
public class PetController {

	public Persistence persistence;

	@Handle(method = "GET", path = "/owners/(\\d+)/pets/new")
	public Object initCreate(long owner) throws IOException {
		var p = new Pet(null, null, null, null, owner);
		return Form.of(p, null, persistence);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/pets/new")
	public Object create(long owner, Pet pet) throws IOException {
		var p = new Pet(null, pet.name(), pet.birthDate(), pet.type(), owner);
		var ee = validate(p);
		if (!ee.isEmpty())
			return Form.of(p, ee, persistence);
		var p2 = persistence.crud(Pet.class).create(p);
		return URI.create("/owners/" + p2.owner());
	}

	@Handle(method = "GET", path = "/owners/(\\d+)/pets/(\\d+)/edit")
	public Object initUpdate(long owner, long id) throws IOException {
		var p = persistence.crud(Pet.class).read(id);
		return Form.of(p, null, persistence);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/pets/(\\d+)/edit")
	public Object update(long owner, long id, Pet pet) throws IOException {
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
			m.computeIfAbsent("name", k -> new ArrayList<>()).add("must not be blank");
		if (pet.birthDate() == null)
			m.computeIfAbsent("birthDate", k -> new ArrayList<>()).add("must not be blank");
		if (pet.type() == null)
			m.computeIfAbsent("type", k -> new ArrayList<>()).add("must not be blank");
		return m;
	}

	@Render(FormRenderer.class)
	public record Form(Owner owner, Pet pet, List<PetType> types, Map<String, List<String>> errors) {

		static Form of(Pet pet, Map<String, List<String>> errors, Persistence persistence) throws IOException {
			var o = persistence.crud(Owner.class).read(pet.owner());
			var tc = persistence.crud(PetType.class);
			var tt = tc.read(tc.filter(null)).toList();
			return new Form(o, pet, tt, errors);
		}
	}

	public static class FormRenderer extends LayoutRenderer<Form> {

		static Map<String, String> labels = Map.of("name", "Name", "birthDate", "Birth Date", "type", "Type");

		@Override
		protected String renderContent(Form form, HttpExchange exchange) {
			var tt = templates("createOrUpdatePetForm.html");
			var h = (form.pet.id() == null ? "New " : "") + "Pet";
			var on = form.owner.firstName() + " " + form.owner.lastName();
			var tt2 = form.types.stream()
					.collect(Collectors.toMap(PetType::id, PetType::name, (y, z) -> y, LinkedHashMap::new));
			var ff = Reflection.properties2(Pet.class)
					.filter(x -> !x.getName().equals("id") && !x.getName().equals("owner")).map(x -> {
						var n = x.getName();
						var l = labels.get(n);
						var v2 = x.get(form.pet);
						var ee = form.errors != null ? form.errors.get(n) : null;
						return n.equals("type") ? new SelectField(l, n, v2, ee, tt2)
								: new InputField(l, n, v2, ee, n.equals("birthDate") ? "date" : "text");
					}).collect(Collectors.toMap(x -> x.name(), x -> x));
			var b = (form.pet.id() == null ? "Add" : "Update") + " Pet";
			return interpolate(tt.get(null), merge(Map.of("heading", h, "ownerName", on), ff, Map.of("button", b)));
		}
	}
}
