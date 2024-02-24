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
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;

import com.janilla.persistence.ApplicationPersistenceBuilder;
import com.janilla.persistence.Persistence;

class CustomPersistenceBuilder extends ApplicationPersistenceBuilder {

	@Override
	public Persistence build() throws IOException {
		var e = Files.exists(file);
		var p = super.build();
		if (!e)
			populate(p);
		return p;
	}

	void populate(Persistence p) throws IOException {
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
			var z = new Owner();
			z.setFirstName(y[0]);
			z.setLastName(y[1]);
			z.setAddress(y[2]);
			z.setCity(y[3]);
			z.setTelephone(y[4]);
			p.getCrud(Owner.class).create(z);
		}

		for (var x : """
				cat
				dog
				lizard
				snake
				bird
				hamster""".split("\n")) {
			var z = new PetType();
			z.setName(x);
			p.getCrud(PetType.class).create(z);
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
			var z = new Pet();
			z.setName(y[0]);
			z.setBirthDate(LocalDate.parse(y[1]));
			z.setType(Long.parseLong(y[2]));
			z.setOwner(Long.parseLong(y[3]));
			p.getCrud(Pet.class).create(z);
		}

		for (var x : """
				7	2013-01-01	rabies shot
				8	2013-01-02	rabies shot
				8	2013-01-03	neutered
				7	2013-01-04	spayed""".split("\n")) {
			var y = x.split("\t");
			var z = new Visit();
			z.setPet(Long.parseLong(y[0]));
			z.setDate(LocalDate.parse(y[1]));
			z.setDescription(y[2]);
			p.getCrud(Visit.class).create(z);
		}

		for (var x : """
				radiology
				surgery
				dentistry""".split("\n")) {
			var z = new Specialty();
			z.setName(x);
			p.getCrud(Specialty.class).create(z);
		}

		for (var x : """
				James	Carter
				Helen	Leary	1
				Linda	Douglas	2	3
				Rafael	Ortega	2
				Henry	Stevens	1
				Sharon	Jenkins""".split("\n")) {
			var y = x.split("\t");
			var z = new Vet();
			z.setFirstName(y[0]);
			z.setLastName(y[1]);
			z.setSpecialties(Arrays.stream(y).skip(2).map(Long::valueOf).toList());
			p.getCrud(Vet.class).create(z);
		}
	}
}
