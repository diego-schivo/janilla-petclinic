/*
 * Copyright 2012-2025 the original author or authors.
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

import com.janilla.web.Render;

/**
 * @author Diego Schivo
 */
@Render(template = "error.html")
public class MethodBlockedException extends RuntimeException {

	private static final long serialVersionUID = 4918046711205707493L;

	public MethodBlockedException() {
		super("The requested action is disabled on this public server: please set up and run the application locally");
	}
}
