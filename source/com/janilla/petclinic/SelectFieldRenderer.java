package com.janilla.petclinic;

import java.util.Map;
import java.util.stream.Collectors;

import com.janilla.http.HttpExchange;
import com.janilla.web.Render;

public class SelectFieldRenderer extends Render.Renderer {

	@Override
	public String apply(Object value, HttpExchange exchange) {
		var tt = templates("selectField.html");
		var v = (SelectField) value;
		var z = v.errors() != null && !v.errors().isEmpty();
		var cn = "form-group " + (z ? "has-error" : "");
		var oo = v.options().map(x -> {
			return interpolate(tt.get("option"), x);
		}).collect(Collectors.joining());
		var fc = "fa fa-" + (z ? "remove" : "ok") + " form-control-feedback";
		var ee = z ? v.errors().stream().map(x -> {
			return interpolate(tt.get("error"), x);
		}).collect(Collectors.joining("<br />")) : "";
		return interpolate(tt.get(null), merge(v, Map.of("className", cn, "options", oo, "feedbackClass", fc, "errors", ee)));
	}
}
