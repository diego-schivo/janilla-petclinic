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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.janilla.persistence.Persistence;
import com.janilla.reflect.Reflection;
import com.janilla.util.Util;
import com.janilla.web.Bind;
import com.janilla.web.Handle;
import com.janilla.web.Render;

/**
 * @author Diego Schivo
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
public class OwnerController {

	protected static final Pattern TEN_DIGITS = Pattern.compile("\\d{10}");

	public Persistence persistence;

	@Handle(method = "GET", path = "/owners/find")
	public Object initFind() {
		return new FindForm(null, null);
	}

	@Handle(method = "GET", path = "/owners")
	public Object find(Owner owner, @Bind("page") Integer page) {
		var oc = persistence.crud(Owner.class);
		var i = page != null ? page - 1 : 0;
		var op = owner.lastName() != null && !owner.lastName().isBlank()
				? oc.filter("lastName", x -> Util.startsWithIgnoreCase((String) x, owner.lastName()), i * 5, 5)
				: oc.list(i * 5, 5);
		return switch ((int) op.total()) {
		case 0 -> new FindForm(owner, Map.of("lastName", List.of("has not been found")));
		case 1 -> URI.create("/owners/" + op.ids()[0]);
		default -> {
			var pc = persistence.crud(Pet.class);
			var l = (int) Math.ceilDiv(op.total(), 5);
			var rr = oc.read(op.ids()).stream().map(o -> {
				var pp = pc.read(pc.filter("owner", o.id()));
				return new FindOutcome.Result(o, pp);
			}).toList();
			var p = new Paginator(i, l, URI.create("/owners"));
			yield new FindOutcome(rr, p);
		}
		};
	}

	@Handle(method = "GET", path = "/owners/(\\d+)")
	public Object show(long id) {
		var oc = persistence.crud(Owner.class);
		var pc = persistence.crud(Pet.class);
		var tc = persistence.crud(PetType.class);
		var vc = persistence.crud(Visit.class);
		var o = oc.read(id);
		var pp = pc.read(pc.filter("owner", o.id())).stream().map(x -> {
			var t = tc.read(x.type());
			var vv = vc.read(vc.filter("pet", x.id()));
			return new Details.Pet2(x, t, vv);
		}).toList();
		return new Details(o, pp);
	}

	@Handle(method = "GET", path = "/owners/new")
	public Object initCreate() {
		return new Form(null, null);
	}

	@Handle(method = "POST", path = "/owners/new")
	public Object create(Owner owner) {
		var ee = validate(owner);
		if (!ee.isEmpty())
			return new Form(owner, ee);
		var o = persistence.crud(Owner.class).create(owner);
		return URI.create("/owners/" + o.id());
	}

	@Handle(method = "GET", path = "/owners/(\\d+)/edit")
	public Object initUpdate(long id) {
		var o = persistence.crud(Owner.class).read(id);
		return new Form(o, null);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/edit")
	public Object update(long id, Owner owner) {
		var ee = validate(owner);
		if (!ee.isEmpty())
			return new Form(owner, ee);
		var o = persistence.crud(Owner.class).update(id, x -> Reflection.copy(owner, x, y -> !y.equals("id")));
		return URI.create("/owners/" + o.id());
	}

	protected Map<String, List<String>> validate(Owner owner) {
		var m = new LinkedHashMap<String, List<String>>();
		if (owner.firstName() == null || owner.firstName().isBlank())
			m.computeIfAbsent("firstName", _ -> new ArrayList<>()).add("must not be blank");
		if (owner.lastName() == null || owner.lastName().isBlank())
			m.computeIfAbsent("lastName", _ -> new ArrayList<>()).add("must not be blank");
		if (owner.address() == null || owner.address().isBlank())
			m.computeIfAbsent("address", _ -> new ArrayList<>()).add("must not be blank");
		if (owner.city() == null || owner.city().isBlank())
			m.computeIfAbsent("city", _ -> new ArrayList<>()).add("must not be blank");
		if (owner.telephone() == null || owner.telephone().isBlank())
			m.computeIfAbsent("telephone", _ -> new ArrayList<>()).add("must not be blank");
		if (owner.telephone() == null || !TEN_DIGITS.matcher(owner.telephone()).matches())
			m.computeIfAbsent("telephone", _ -> new ArrayList<>())
					.add("numeric value out of bounds (<10 digits>.<0 digits> expected)");
		return m;
	}

	@Render(template = "findOwners.html")
	public record FindForm(Owner owner, Map<String, List<String>> errors) {
	}

	@Render(template = "ownersList.html")
	public record FindOutcome(List<Result> results, Paginator paginator) {

		@Render(template = "result")
		public record Result(Owner owner, @Render(delimiter = ", ") List<@Render(template = "pet") Pet> pets) {
		}
	}

	@Render(template = "ownerDetails.html")
	public record Details(Owner owner, List<Pet2> pets) {

		@Render(template = "pet")
		public record Pet2(Pet pet, PetType type, List<@Render(template = "visit") Visit> visits) {
		}
	}

	@Render(template = "createOrUpdateOwnerForm.html")
	public record Form(Owner owner, Map<String, List<String>> errors) {

		private static final Map<String, String> LABELS = Map.of("firstName", "First Name", "lastName", "Last Name",
				"address", "Address", "city", "City", "telephone", "Telephone");

		public Function<String, FormField<?>> fields() {
			return x -> {
				var l = LABELS.get(x);
				var v = Reflection.property(Owner.class, x).get(owner);
				var ee = errors != null ? errors.get(x) : null;
				return new InputField<>(l, x, (String) v, ee, "text");
			};
		}

		public String button() {
			return owner == null || owner.id() == null ? "Add Owner" : "Update Owner";
		}
	}
}
