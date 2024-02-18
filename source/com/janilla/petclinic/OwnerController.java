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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.janilla.persistence.Persistence;
import com.janilla.petclinic.OwnerController.Details.Pet2;
import com.janilla.petclinic.OwnerController.FindOutcome.Result;
import com.janilla.reflect.Parameter;
import com.janilla.reflect.Reflection;
import com.janilla.util.Util;
import com.janilla.web.Handle;
import com.janilla.web.Render;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Diego Schivo
 */
public class OwnerController {

	private Persistence persistence;

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", uri = "/owners/find")
	public Object initFind() {
		var o = new Owner();
		return new FindForm(o, null);
	}

	@Handle(method = "GET", uri = "/owners")
	public Object find(Owner owner, @Parameter(name = "page") Integer page) throws IOException {
		var c = persistence.getCrud(Owner.class);
		var n = owner.getLastName();
		var i = page != null ? page - 1 : 0;
		var f = n != null && !n.isBlank()
				? c.filter2("lastName", i * 5, 5, x -> Util.startsWithIgnoreCase((String) x, n))
				: c.list(i * 5, 5);
		return switch ((int) f.total()) {
		case 0 -> new FindForm(owner, Map.of("lastName", List.of("has not been found")));
		case 1 -> URI.create("/owners/" + f.ids()[0]);
		default -> {
			var d = persistence.getCrud(Pet.class);
			var l = (int) ((f.total() + 4) / 5);
//			try {
			var r = c.read(f.ids()).map(o -> {
				try {
					var p = d.read(d.filter("owner", o.getId())).toList();
					return new Result(o, p);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}).toList();
			var p = new Paginator(i, l, URI.create("/owners"));
			yield new FindOutcome(r, p);
//			} catch (UncheckedIOException e) {
//				throw e.getCause();
//			}
		}
		};
	}

	@Handle(method = "GET", uri = "/owners/(\\d+)")
	public Object show(long id) throws IOException {
		var c = persistence.getCrud(Owner.class);
		var d = persistence.getCrud(Pet.class);
		var e = persistence.getCrud(Visit.class);
		var o = c.read(id);
		var p = d.read(d.filter("owner", o.getId())).map(x -> {
			try {
				var t = persistence.getCrud(PetType.class).read(x.getType());
				var v = e.read(e.filter("pet", x.getId()));
				return new Pet2(x, t, v);
			} catch (IOException f) {
				throw new UncheckedIOException(f);
			}
		});
		return new Details(o, p);
	}

	@Handle(method = "GET", uri = "/owners/new")
	public Object initCreate() {
		var o = new Owner();
		return new Form(o, null);
	}

	@Handle(method = "POST", uri = "/owners/new")
	public Object create(Owner owner) throws IOException {
		var errors = validate(owner);
		if (!errors.isEmpty())
			return new Form(owner, errors);

		var i = persistence.getCrud(Owner.class).create(owner);
		return URI.create("/owners/" + i);
	}

	@Handle(method = "GET", uri = "/owners/(\\d+)/edit")
	public Object initUpdate(long id) throws IOException {
		var c = persistence.getCrud(Owner.class);
		var o = c.read(id);
		return new Form(o, null);
	}

	@Handle(method = "POST", uri = "/owners/(\\d+)/edit")
	public Object update(long id, Owner owner) throws IOException {
		var errors = validate(owner);
		if (!errors.isEmpty())
			return new Form(owner, errors);

		var o = persistence.getCrud(Owner.class).update(id, x -> Reflection.copy(owner, x, y -> !y.equals("id")));
		return URI.create("/owners/" + o.getId());
	}

	static Pattern tenDigits = Pattern.compile("\\d{10}");

	protected Map<String, Collection<String>> validate(Owner owner) {
		var errors = new HashMap<String, Collection<String>>();
		if (owner.getFirstName() == null || owner.getFirstName().isBlank())
			errors.computeIfAbsent("firstName", k -> new ArrayList<>()).add("must not be blank");
		if (owner.getLastName() == null || owner.getLastName().isBlank())
			errors.computeIfAbsent("lastName", k -> new ArrayList<>()).add("must not be blank");
		if (owner.getAddress() == null || owner.getAddress().isBlank())
			errors.computeIfAbsent("address", k -> new ArrayList<>()).add("must not be blank");
		if (owner.getCity() == null || owner.getCity().isBlank())
			errors.computeIfAbsent("city", k -> new ArrayList<>()).add("must not be blank");
		if (owner.getTelephone() == null || owner.getTelephone().isBlank())
			errors.computeIfAbsent("telephone", k -> new ArrayList<>()).add("must not be blank");
		if (owner.getTelephone() == null || !tenDigits.matcher(owner.getTelephone()).matches())
			errors.computeIfAbsent("telephone", k -> new ArrayList<>())
					.add("numeric value out of bounds (<10 digits>.<0 digits> expected)");
		return errors;
	}

	@Render(template = "findOwners.html")
	public record FindForm(Owner owner, Map<String, @Render(template = """
			<div>#{}</div>
			""") Collection<@Render(template = """
			<p>${}</p>
			""") String>> errors) {
	}

	@Render(template = "ownersList.html")
	public record FindOutcome(Collection<Result> results, Paginator paginator) {

		@Render(template = "ownersList-result.html")
		public record Result(Owner owner, @Render(delimiter = ", ") Collection<@Render(template = """
				${name}
				""") Pet> pets) {
		}
	}

	@Render(template = "ownerDetails.html")
	public record Details(Owner owner, Stream<Pet2> pets) {

		@Render(template = "ownerDetails-pet.html")
		public record Pet2(Pet pet, PetType type, Stream<@Render(template = "ownerDetails-visit.html") Visit> visits) {
		}
	}

	@Render(template = "createOrUpdateOwnerForm.html")
	public record Form(Owner owner, Map<String, Collection<String>> errors) {

		static Map<String, String> labels = Map.of("firstName", "First Name", "lastName", "Last Name", "address",
				"Address", "city", "City", "telephone", "Telephone");

		public Function<String, FormField> fields() {
			return n -> {
				try {
					var l = labels.get(n);
					var v = Reflection.getter(Owner.class, n).invoke(owner);
					var e = errors != null ? errors.get(n) : null;
					return new InputField(l, n, "text", v, e);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			};
		}

		public String button() {
			return (owner.getId() == null ? "Add" : "Update") + " Owner";
		}
	}
}
