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

import java.net.URI;

import com.janilla.net.Net;
import com.janilla.util.EntryList;
import com.janilla.web.Render;

/**
 * @author Diego Schivo
 */
@Render(PaginatorRenderer.class)
public record Paginator(int index, int length, URI uri) {

	public URI uri(int page) {
		var el = Net.parseQueryString(uri.getRawQuery());
		if (el == null)
			el = new EntryList<>();
		el.set("page", String.valueOf(page));
		return URI.create(uri.getPath() + "?" + Net.formatQueryString(el));
	}
}
