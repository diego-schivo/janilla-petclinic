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

import com.janilla.web.Render;

/**
 * @author Diego Schivo
 */
@Render("inputField.html")
public record InputField(String label, String name, String type, Object value, @Render(template = """
		<span class="help-inline">{}</span>
		""", delimiter = "<br />") Collection<String> errors) implements FormField {

	public String errorClass() {
		return errors == null || errors.isEmpty() ? "" : "has-error";
	}

	public String feedbackIcon() {
		return errors == null || errors.isEmpty() ? "ok" : "remove";
	}
}
