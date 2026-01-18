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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

import com.janilla.ioc.DiFactory;
import com.janilla.backend.persistence.ApplicationPersistenceBuilder;
import com.janilla.backend.persistence.Persistence;

/**
 * @author Diego Schivo
 */
public class CustomPersistenceBuilder extends ApplicationPersistenceBuilder {

	public CustomPersistenceBuilder(Path databaseFile, DiFactory diFactory) {
		super(databaseFile, diFactory);
	}

	@Override
	public Persistence build() {
		var fe = Files.exists(databaseFile);
		var p = super.build();
//		p.setTypeResolver(x -> {
//			try {
//				return Class.forName(getClass().getPackageName() + "." + x.replace('.', '$'));
//			} catch (ClassNotFoundException e) {
//				throw new RuntimeException(e);
//			}
//		});
		if (!fe)
			seed(p);
		return p;
	}

	private void seed(Persistence persistence) {
		for (var x : """
				George	Franklin	110 W. Liberty St.	Madison	6085551023
				Betty	Davis	638 Cardinal Ave.	Sun Prairie	6085551749
				Eduardo	Rodriquez	2693 Commerce St.	McFarland	6085558763
				Harold	Davis	563 Friendly St.	Windsor	6085553198
				Peter	McTavish	2387 S. Fair Way	Madison	6085552765
				Jean	Coleman	105 N. Lake St.	Monona	6085552654
				Jeff	Black	1450 Oak Blvd.	Monona	6085555387
				Maria	Escobito	345 Maple St.	Madison	6085557683
				David	Schroeder	2749 Blackhawk Trail	Madison	6085559435
				Carlos	Estaban	2335 Independence La.	Waunakee	6085555487""".split("\n")) {
			var y = x.split("\t");
			persistence.crud(Owner.class).create(new Owner(null, y[0], y[1], y[2], y[3], y[4]));
		}

		for (var x : """
				cat
				dog
				lizard
				snake
				bird
				hamster""".split("\n")) {
			persistence.crud(PetType.class).create(new PetType(null, x));
		}

		for (var x : """
				Leo	2010-09-07	1	1
				Basil	2012-08-06	6	2
				Rosy	2011-04-17	2	3
				Jewel	2010-03-07	2	3
				Iggy	2010-11-30	3	4
				George	2010-01-20	4	5
				Samantha	2012-09-04	1	6
				Max	2012-09-04	1	6
				Lucky	2011-08-06	5	7
				Mulligan	2007-02-24	2	8
				Freddy	2010-03-09	5	9
				Lucky	2010-06-24	2	10
				Sly	2012-06-08	1	10""".split("\n")) {
			var y = x.split("\t");
			persistence.crud(Pet.class)
					.create(new Pet(null, y[0], LocalDate.parse(y[1]), Long.parseLong(y[2]), Long.parseLong(y[3])));
		}

		for (var x : """
				7	2013-01-01	rabies shot
				8	2013-01-02	rabies shot
				8	2013-01-03	neutered
				7	2013-01-04	spayed""".split("\n")) {
			var y = x.split("\t");
			persistence.crud(Visit.class).create(new Visit(null, Long.parseLong(y[0]), LocalDate.parse(y[1]), y[2]));
		}

		for (var x : """
				radiology
				surgery
				dentistry""".split("\n")) {
			persistence.crud(Specialty.class).create(new Specialty(null, x));
		}

		for (var x : """
				James	Carter
				Helen	Leary	1
				Linda	Douglas	2	3
				Rafael	Ortega	2
				Henry	Stevens	1
				Sharon	Jenkins""".split("\n")) {
			var y = x.split("\t");
			persistence.crud(Vet.class)
					.create(new Vet(null, y[0], y[1], Arrays.stream(y).skip(2).map(Long::valueOf).toList()));
		}
	}
}
