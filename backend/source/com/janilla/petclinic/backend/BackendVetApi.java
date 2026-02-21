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
import com.janilla.persistence.ListPortion;
import com.janilla.petclinic.Vet;
import com.janilla.petclinic.VetApi;
import com.janilla.web.Handle;

@Handle(path = "/api/vets")
public class BackendVetApi implements VetApi {

	protected final Persistence persistence;

	public BackendVetApi(Persistence persistence) {
		this.persistence = persistence;
	}

	@Override
	@Handle(method = "GET")
	public ListPortion<Vet> read(Integer depth, Integer skip, Integer limit) {
		var c = persistence.crud(Vet.class);
		var lp = c.list(skip != null ? skip : 0, limit != null ? limit : 0);
		return new ListPortion<>(c.read(lp.elements(), depth != null ? depth : 0), lp.totalSize());
	}
}
