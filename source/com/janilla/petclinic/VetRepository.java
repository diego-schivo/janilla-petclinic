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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.janilla.io.IO;
import com.janilla.persistence.Crud;

/**
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Diego Schivo
 */
public class VetRepository extends Crud<Vet> {

	Map<Long, IO.Supplier<Vet>> readCache = new ConcurrentHashMap<>();

	IO.Supplier<long[]> listCache1 = IO.Lazy.of(() -> super.list());

	Map<List<Long>, IO.Supplier<Page>> listCache2 = new ConcurrentHashMap<>();

	@Override
	public Vet read(long id) throws IOException {
		return readCache.computeIfAbsent(id, k -> IO.Lazy.of(() -> super.read(id))).get();
	}

	@Override
	public long[] list() throws IOException {
		return listCache1.get();
	}

	@Override
	public Page list(long skip, long limit) throws IOException {
		return listCache2.computeIfAbsent(List.of(skip, limit), k -> IO.Lazy.of(() -> super.list(skip, limit))).get();
	}
}
