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
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

import com.janilla.http.HttpServer;
import com.janilla.io.IO;
import com.janilla.persistence.Persistence;
import com.janilla.persistence.PersistenceBuilder;
import com.janilla.util.Util;
import com.janilla.web.ApplicationHandlerBuilder;

/**
 * @author Dave Syer
 * @author Diego Schivo
 */
public class PetClinicApplication {

	public static void main(String[] args) throws IOException {
		var a = new PetClinicApplication();
		a.getPersistence();

		var s = new HttpServer();
		s.setPort(8080);
		{
			var b = new ApplicationHandlerBuilder();
			b.setApplication(a);
			s.setHandler(b.build());
		}
		s.run();
	}

	private IO.Supplier<Persistence> persistence = IO.Lazy.of(() -> {
		var f = Path.of(System.getProperty("user.home"), ".janilla", "petclinic", "petclinic.database");
		var e = Files.exists(f);

		var b = new PersistenceBuilder();
		b.setFile(f);
		b.setTypes(() -> Util.getPackageClasses("com.janilla.petclinic").iterator());
		var p = b.build();

		if (!e) {
			{
				var c = p.getCrud(Owner.class);
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
					p.getDatabase().performTransaction(() -> c.create(z));
				}
			}
			{
				var c = p.getCrud(PetType.class);
				for (var x : """
						cat
						dog
						lizard
						snake
						bird
						hamster""".split("\n")) {
					var z = new PetType();
					z.setName(x);
					p.getDatabase().performTransaction(() -> c.create(z));
				}
			}
			{
				var c = p.getCrud(Pet.class);
				for (var x : """
						Leo	2010-09-07	0	0
						Basil	2012-08-06	5	1
						Rosy	2011-04-17	1	2
						Jewel	2010-03-07	1	2
						Iggy	2010-11-30	2	3
						George	2010-01-20	3	4
						Samantha	2012-09-04	0	5
						Max	2012-09-04	0	5
						Lucky	2011-08-06	4	6
						Mulligan	2007-02-24	1	7
						Freddy	2010-03-09	4	8
						Lucky	2010-06-24	1	9
						Sly	2012-06-08	0	9""".split("\n")) {
					var y = x.split("\t");
					var z = new Pet();
					z.setName(y[0]);
					z.setBirthDate(LocalDate.parse(y[1]));
					z.setType(Long.parseLong(y[2]));
					z.setOwner(Long.parseLong(y[3]));
					p.getDatabase().performTransaction(() -> c.create(z));
				}
			}
			{
				var c = p.getCrud(Visit.class);
				for (var x : """
						6	2013-01-01	rabies shot
						7	2013-01-02	rabies shot
						7	2013-01-03	neutered
						6	2013-01-04	spayed""".split("\n")) {
					var y = x.split("\t");
					var z = new Visit();
					z.setPet(Long.parseLong(y[0]));
					z.setDate(LocalDate.parse(y[1]));
					z.setDescription(y[2]);
					p.getDatabase().performTransaction(() -> c.create(z));
				}
			}
			{
				var c = p.getCrud(Specialty.class);
				for (var x : """
						radiology
						surgery
						dentistry""".split("\n")) {
					var z = new Specialty();
					z.setName(x);
					p.getDatabase().performTransaction(() -> c.create(z));
				}
			}
			{
				var c = p.getCrud(Vet.class);
				for (var x : """
						James	Carter
						Helen	Leary	0
						Linda	Douglas	1	2
						Rafael	Ortega	1
						Henry	Stevens	0
						Sharon	Jenkins""".split("\n")) {
					var y = x.split("\t");
					var z = new Vet();
					z.setFirstName(y[0]);
					z.setLastName(y[1]);
					z.setSpecialties(Arrays.stream(y).skip(2).map(Long::valueOf).toList());
					p.getDatabase().performTransaction(() -> c.create(z));
				}
			}
		}

		return p;
	});

	public Persistence getPersistence() throws IOException {
		return persistence.get();
	}
}
