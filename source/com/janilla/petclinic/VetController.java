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
import java.util.Collection;

import com.janilla.persistence.Persistence;
import com.janilla.petclinic.VetController.FindOutcome.Result;
import com.janilla.reflect.Parameter;
import com.janilla.web.Handle;
import com.janilla.web.Render;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Diego Schivo
 */
public class VetController {

	private Persistence persistence;

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "/vets.html")
	public Object find(@Parameter(name = "page") Integer page) throws IOException {
		try {
			var c = persistence.getCrud(Vet.class);
			var i = page != null ? page - 1 : 0;
			var p = c.list(i * 5, 5);
			var l = (int) ((p.total() + 4) / 5);
			var r = c.read(p.ids()).map(v -> {
				var j = v.specialties().stream().mapToLong(Long::longValue).toArray();
				var s = persistence.getCrud(Specialty.class).read(j).toList();
				return new Result(v, s);
			}).toList();
			var q = new Paginator(i, l, URI.create("/vets.html"));
			return new FindOutcome(r, q);
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	@Handle(method = "GET", path = "/vets")
	public Object find() throws IOException {
		var c = persistence.getCrud(Vet.class);
		var v = c.read(c.list()).toList();
//		return new Vets(v);
		return v;
	}

	@Render("vetList.html")
	public record FindOutcome(Collection<Result> results, Paginator paginator) {

		@Render("vetList-result.html")
		public record Result(Vet vet, @Render(delimiter = ", ") Collection<@Render("""
				<span>{name}</span>
				""") Specialty> specialties) {
		}
	}
}
