package com.janilla.petclinic.frontend;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.janilla.java.Reflection;
import com.janilla.petclinic.Pet;
import com.janilla.petclinic.PetType;
import com.janilla.web.Render;

@Render(template = "createOrUpdatePetForm.html")
public record PetForm(Pet pet, List<PetType> types, Map<String, List<String>> errors) {

	private static final Map<String, String> LABELS = Map.of("name", "Name", "birthDate", "Birth Date", "type", "Type");

	public String heading() {
		return pet.id() == null ? "New Pet" : "Pet";
	}

	public Function<String, FormField<?>> fields() {
		return x -> {
			var l = LABELS.get(x);
			var v = Reflection.property(Pet.class, x).get(pet);
			var ee = errors != null ? errors.get(x) : null;
			return switch (x) {
			case "type" -> {
				var ii = types.stream()
						.collect(Collectors.toMap(PetType::id, PetType::name, (y, _) -> y, LinkedHashMap::new));
				yield new SelectField<>(l, x + ".id", ((PetType) v).id(), ee, ii);
			}
			case "birthDate" -> new InputField<>(l, x, (LocalDate) v, ee, "date");
			default -> new InputField<>(l, x, (String) v, ee, "text");
			};
		};
	}

	public String button() {
		return pet.id() == null ? "Add Pet" : "Update Pet";
	}
}
