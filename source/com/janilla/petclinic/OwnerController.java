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

	public Persistence persistence;

	@Handle(method = "GET", path = "/owners/find")
	public Object initFind() {
		return new FindForm(null, null);
	}

	@Handle(method = "GET", path = "/owners")
	public Object find(Owner owner, @Bind("page") Integer page) throws IOException {
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
			var rr = oc.read(op.ids()).map(o -> {
				var pp = pc.read(pc.filter("owner", o.id())).toList();
				return new FindOutcome.Result(o, pp);
			}).toList();
			var p = new Paginator(i, l, URI.create("/owners"));
			yield new FindOutcome(rr, p);
		}
		};
	}

	@Handle(method = "GET", path = "/owners/(\\d+)")
	public Object show(long id) throws IOException {
		var oc = persistence.crud(Owner.class);
		var pc = persistence.crud(Pet.class);
		var tc = persistence.crud(PetType.class);
		var vc = persistence.crud(Visit.class);
		var o = oc.read(id);
		var pp = pc.read(pc.filter("owner", o.id())).map(x -> {
			var t = tc.read(x.type());
			var vv = vc.read(vc.filter("pet", x.id())).toList();
			return new Details.Pet2(x, t, vv);
		}).toList();
		return new Details(o, pp);
	}

	@Handle(method = "GET", path = "/owners/new")
	public Object initCreate() {
		return new Form(null, null);
	}

	@Handle(method = "POST", path = "/owners/new")
	public Object create(Owner owner) throws IOException {
		var ee = validate(owner);
		if (!ee.isEmpty())
			return new Form(owner, ee);
		var o = persistence.crud(Owner.class).create(owner);
		return URI.create("/owners/" + o.id());
	}

	@Handle(method = "GET", path = "/owners/(\\d+)/edit")
	public Object initUpdate(long id) throws IOException {
		var o = persistence.crud(Owner.class).read(id);
		return new Form(o, null);
	}

	@Handle(method = "POST", path = "/owners/(\\d+)/edit")
	public Object update(long id, Owner owner) throws IOException {
		var ee = validate(owner);
		if (!ee.isEmpty())
			return new Form(owner, ee);
		var o = persistence.crud(Owner.class).update(id, x -> Reflection.copy(owner, x, y -> !y.equals("id")));
		return URI.create("/owners/" + o.id());
	}

	static Pattern tenDigits = Pattern.compile("\\d{10}");

	protected Map<String, List<String>> validate(Owner owner) {
		var m = new LinkedHashMap<String, List<String>>();
		if (owner.firstName() == null || owner.firstName().isBlank())
			m.computeIfAbsent("firstName", k -> new ArrayList<>()).add("must not be blank");
		if (owner.lastName() == null || owner.lastName().isBlank())
			m.computeIfAbsent("lastName", k -> new ArrayList<>()).add("must not be blank");
		if (owner.address() == null || owner.address().isBlank())
			m.computeIfAbsent("address", k -> new ArrayList<>()).add("must not be blank");
		if (owner.city() == null || owner.city().isBlank())
			m.computeIfAbsent("city", k -> new ArrayList<>()).add("must not be blank");
		if (owner.telephone() == null || owner.telephone().isBlank())
			m.computeIfAbsent("telephone", k -> new ArrayList<>()).add("must not be blank");
		if (owner.telephone() == null || !tenDigits.matcher(owner.telephone()).matches())
			m.computeIfAbsent("telephone", k -> new ArrayList<>())
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

		static Map<String, String> labels = Map.of("firstName", "First Name", "lastName", "Last Name", "address",
				"Address", "city", "City", "telephone", "Telephone");

		public Function<String, FormField> fields() {
			return x -> {
				var l = labels.get(x);
				var v = Reflection.property(Owner.class, x).get(owner);
				var ee = errors != null ? errors.get(x) : null;
				return new InputField(l, x, v, ee, "text");
			};
		}

		public String button() {
			return (owner == null || owner.id() == null ? "Add" : "Update") + " Owner";
		}
	}

//	public static class FormRenderer extends LayoutRenderer<Form> {
//
//		static Map<String, String> labels = Map.of("firstName", "First Name", "lastName", "Last Name", "address",
//				"Address", "city", "City", "telephone", "Telephone");
//
//		@Override
//		protected String renderContent(Form form, HttpExchange exchange) {
//			var tt = templates("createOrUpdateOwnerForm.html");
//			var ff = Reflection.properties2(Owner.class).filter(x -> !x.getName().equals("id")).map(x -> {
//				var n = x.getName();
//				var l = labels.get(n);
//				var v2 = x.get(form.owner);
//				var ee = form.errors != null ? form.errors.get(n) : null;
//				return new InputField(l, n, v2, ee, "text");
//			}).collect(Collectors.toMap(x -> x.name(), x -> x));
//			var b = (form.owner == null || form.owner.id() == null ? "Add" : "Update") + " Owner";
//			return interpolate(tt.get(null), merge(ff, Map.of("button", b)));
//		}
//	}
}
