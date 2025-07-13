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

import java.util.Collection;

import com.janilla.database.Database;
import com.janilla.json.TypeResolver;
import com.janilla.persistence.Crud;
import com.janilla.persistence.Entity;
import com.janilla.persistence.Persistence;

/**
 * @author Diego Schivo
 */
public class CustomPersistence extends Persistence {

	public CustomPersistence(Database database, Collection<Class<? extends Entity<?>>> types,
			TypeResolver typeResolver) {
		super(database, types, typeResolver);
	}

	@Override
	protected <E extends Entity<?>> Crud<?, E> newCrud(Class<E> type) {
		if (type == Vet.class) {
			@SuppressWarnings("unchecked")
			var c = (Crud<?, E>) new VetRepository(this);
			return c;
		}
		if (type == Specialty.class) {
			@SuppressWarnings("unchecked")
			var c = (Crud<?, E>) new SpecialtyRepository(this);
			return c;
		}
		return super.newCrud(type);
	}
}
