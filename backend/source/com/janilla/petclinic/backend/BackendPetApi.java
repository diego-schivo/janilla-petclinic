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

import java.util.Set;

import com.janilla.backend.persistence.Persistence;
import com.janilla.java.Reflection;
import com.janilla.petclinic.Pet;
import com.janilla.petclinic.PetApi;
import com.janilla.web.Handle;

@Handle(path = "/api/pets")
public class BackendPetApi implements PetApi {

	protected final Persistence persistence;

	public BackendPetApi(Persistence persistence) {
		this.persistence = persistence;
	}

	@Override
	@Handle(method = "POST")
	public Pet create(Pet pet) {
//		IO.println("PetApi.create, pet=" + pet);
		return persistence.crud(Pet.class).create(pet);
	}

	@Override
	@Handle(method = "GET", path = "(\\d+)")
	public Pet read(Long id, Integer depth) {
		return persistence.crud(Pet.class).read(id, depth != null ? depth : 0);
	}

	@Override
	@Handle(method = "PUT", path = "(\\d+)")
	public Pet update(Long id, Pet pet) {
//		IO.println("PetApi.update, id=" + id + ", owner=" + owner);
		return persistence.crud(Pet.class).update(id,
				x -> Reflection.copy(pet, x, y -> !Set.of("id", "owner").contains(y)));
	}
}
