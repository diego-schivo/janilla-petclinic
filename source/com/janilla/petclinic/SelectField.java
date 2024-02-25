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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.janilla.web.Render;

/**
 * @author Diego Schivo
 */
@Render(template = "selectField.html")
public record SelectField(String label, String name, Map<? extends Object, String> items, Object value,
		@Render(template = """
				<span class="help-inline">____</span>
				""", delimiter = "<br />") Collection<String> errors) implements FormField {

	public String errorClass() {
		return errors == null || errors.isEmpty() ? "" : "has-error";
	}

	public Stream<Option> options() {
		return items.entrySet().stream()
				.map(e -> new Option(e.getKey(), e.getValue(), Objects.equals(e.getKey(), value)));
	}

	public String feedbackIcon() {
		return errors == null || errors.isEmpty() ? "ok" : "remove";
	}

	@Render(template = """
			<option value="__value__" <!--__selectedAttribute__-->>__text__</option>
			""")
	public record Option(Object value, String text, boolean selected) {

		public String selectedAttribute() {
			return selected ? "selected=\"selected\"" : "";
		}
	}
}
