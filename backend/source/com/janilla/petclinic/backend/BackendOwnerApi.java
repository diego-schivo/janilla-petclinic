/*
 * MIT License
 *
 * Copyright (c) 2024 Vercel, Inc.
 * Copyright (c) 2024-2026 Diego Schivo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.janilla.petclinic.backend;

import com.janilla.backend.persistence.Persistence;
import com.janilla.java.Reflection;
import com.janilla.persistence.ListPortion;
import com.janilla.petclinic.Owner;
import com.janilla.petclinic.OwnerApi;
import com.janilla.web.Handle;

@Handle(path = "/api/owners")
public class BackendOwnerApi implements OwnerApi {

	protected final Persistence persistence;

	public BackendOwnerApi(Persistence persistence) {
		this.persistence = persistence;
	}

	@Override
	@Handle(method = "POST")
	public Owner create(Owner owner) {
//		IO.println("OwnerApi.create, owner=" + owner);
		return persistence.crud(Owner.class).create(owner);
	}

	@Override
	@Handle(method = "GET")
	public ListPortion<Owner> read(String lastName, Integer depth, Integer skip, Integer limit) {
//		IO.println("BackendOwnerApi.read, lastName=" + lastName + ", depth=" + depth + ", skip=" + skip + ", limit="
//				+ limit);
		var c = persistence.crud(Owner.class);
		var lp = lastName != null && !lastName.isBlank() ? c.filter("lastName",
				x -> startsWithIgnoreCase((String) x, lastName), skip != null ? skip : 0, limit != null ? limit : -1)
				: c.list(skip != null ? skip : 0, limit != null ? limit : -1);
		return new ListPortion<>(c.read(lp.elements(), depth != null ? depth : 0), lp.totalSize());
	}

	@Override
	@Handle(method = "GET", path = "(\\d+)")
	public Owner read(Long id, Integer depth) {
		return persistence.crud(Owner.class).read(id, depth != null ? depth : 0);
	}

	@Override
	@Handle(method = "PUT", path = "(\\d+)")
	public Owner update(Long id, Owner owner) {
//		IO.println("OwnerApi.update, id=" + id + ", owner=" + owner);
		return persistence.crud(Owner.class).update(id, x -> Reflection.copy(owner, x, y -> !y.equals("id")));
	}

	protected static boolean startsWithIgnoreCase(String string, String prefix) {
		return string == prefix || (prefix != null && prefix.length() <= string.length()
				&& string.regionMatches(true, 0, prefix, 0, prefix.length()));
	}
}
