/*
 * Copyright 2012-2026 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.janilla.backend.persistence.Crud;
import com.janilla.backend.persistence.IdPage;
import com.janilla.backend.persistence.Persistence;

/**
 * @author Diego Schivo
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public class VetRepository extends Crud<Long, Vet> {

	Map<Long, Supplier<Vet>> readCache = new ConcurrentHashMap<>();

	Supplier<List<Long>> listCache1 = Lazy.of(() -> super.list());

	Map<List<Long>, Supplier<IdPage<Long>>> listCache2 = new ConcurrentHashMap<>();

	public VetRepository(Persistence persistence) {
		super(Vet.class, null, persistence);
	}

	@Override
	public Vet read(Long id) {
		return readCache.computeIfAbsent(id, _ -> Lazy.of(() -> super.read(id))).get();
	}

	@Override
	public List<Long> list() {
		return listCache1.get();
	}

	@Override
	public IdPage<Long> list(long skip, long limit) {
		return listCache2.computeIfAbsent(List.of(skip, limit), _ -> Lazy.of(() -> super.list(skip, limit))).get();
	}
}
