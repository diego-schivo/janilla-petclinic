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

import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;

import com.janilla.web.Render;
import com.janilla.web.RenderableFactory;

/**
 * @author Diego Schivo
 */
public class CustomRenderableFactory extends RenderableFactory {

	protected final PetClinicApplication application;

	public CustomRenderableFactory(PetClinicApplication application) {
		this.application = application;
	}

	@Override
	protected <T> Render annotation(AnnotatedElement annotated, T value) {
		var x = super.annotation(annotated, value);
		if (x == null && value instanceof Exception)
			x = ShowcaseException.class.getAnnotation(Render.class);
		return x;
	}

	@Override
	protected <T> InputStream getResourceAsStream(T value, String name) {
		return application.getClass().getResourceAsStream(name);
	}
}
