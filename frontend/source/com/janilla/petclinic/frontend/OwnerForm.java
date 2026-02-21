package com.janilla.petclinic.frontend;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.janilla.java.Reflection;
import com.janilla.petclinic.Owner;
import com.janilla.web.Render;

@Render(template = "createOrUpdateOwnerForm.html")
public record OwnerForm(Owner owner, Map<String, List<String>> errors) {

	private static final Map<String, String> LABELS = Map.of("firstName", "First Name", "lastName", "Last Name",
			"address", "Address", "city", "City", "telephone", "Telephone");

	public Function<String, FormField<?>> fields() {
		return x -> {
			var l = LABELS.get(x);
			var v = Reflection.property(Owner.class, x).get(owner);
			var ee = errors != null ? errors.get(x) : null;
			return new InputField<>(l, x, (String) v, ee, "text");
		};
	}

	public String button() {
		return owner == null || owner.id() == null ? "Add Owner" : "Update Owner";
	}
}
