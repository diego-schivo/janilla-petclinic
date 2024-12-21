package com.janilla.petclinic;

import java.util.Map;
import java.util.stream.Collectors;

import com.janilla.http.HttpExchange;
import com.janilla.web.Render;

public class InputFieldRenderer extends Render.Renderer {

	@Override
	public String apply(Object value, HttpExchange exchange) {
		var tt = templates("inputField.html");
		var v = (InputField) value;
		var z = v.errors() != null && !v.errors().isEmpty();
		var cn = "form-group " + (z ? "has-error" : "");
		var fc = "fa fa-" + (z ? "remove" : "ok") + " form-control-feedback";
		var ee = z ? v.errors().stream().map(x -> {
			return interpolate(tt.get("error"), x);
		}).collect(Collectors.joining("<br />")) : "";
		return interpolate(tt.get(null), merge(v, Map.of("className", cn, "feedbackClass", fc, "errors", ee)));
	}
}
