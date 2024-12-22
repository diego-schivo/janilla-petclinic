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
