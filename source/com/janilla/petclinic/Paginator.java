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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.janilla.net.Net;
import com.janilla.util.EntryList;
import com.janilla.web.Render;

/**
 * @author Diego Schivo
 */
@Render(template = "paginator.html")
public record Paginator(int index, int length, URI uri) {

	public Stream<@Render(template = "paginator-page.html") Element> pages() {
		return IntStream.rangeClosed(1, length).mapToObj(x -> new Element(x != index + 1 ? uri : null, x));
	}

	public Paginator.Element first() {
		return new Element(index != 0 ? uri : null, 1);
	}

	public Paginator.Element previous() {
		return new Element(index > 0 ? uri : null, index);
	}

	public Paginator.Element next() {
		return new Element(index < length - 1 ? uri : null, index + 2);
	}

	public Paginator.Element last() {
		return new Element(index != length - 1 ? uri : null, length);
	}

	public record Element(URI uri, int page) {

		public String tagName() {
			return uri != null ? "a" : "span";
		}

		public URI href() {
			if (uri == null)
				return null;
			var l = Net.parseQueryString(uri.getRawQuery());
			if (l == null)
				l = new EntryList<>();
			l.set("page", String.valueOf(page));
			return URI.create(uri.getPath() + "?" + Net.formatQueryString(l));
		}
	}
}
