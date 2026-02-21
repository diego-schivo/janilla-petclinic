package com.janilla.petclinic.frontend;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.janilla.java.Reflection;
import com.janilla.petclinic.Visit;
import com.janilla.web.Render;

@Render(template = "createOrUpdateVisitForm.html")
public record VisitForm(Visit visit, List<@Render(template = "visit") Visit> previousVisits,
		Map<String, List<String>> errors) {

	private static final Map<String, String> LABELS = Map.of("date", "Date", "description", "Description");

	public Function<String, FormField<?>> fields() {
		return x -> {
			var l = LABELS.get(x);
			var v = Reflection.property(Visit.class, x).get(visit);
			var ee = errors != null ? errors.get(x) : null;
			return switch (x) {
			case "date" -> new InputField<>(l, x, (LocalDate) v, ee, "date");
			default -> new InputField<>(l, x, (String) v, ee, "text");
			};
		};
	}
}
