package com.janilla.petclinic.frontend;

import java.util.List;

import com.janilla.petclinic.Specialty;
import com.janilla.petclinic.Vet;
import com.janilla.web.HtmlRenderer;
import com.janilla.web.Render;

@Render(template = "vetList.html")
public record VetList(List<Result> results, Paginator paginator) {

	@Render(template = "result")
	public record Result(Vet vet,
			@Render(renderer = SpecialtiesRenderer.class) List<@Render(template = "specialty") Specialty> specialties) {
	}

	public static class SpecialtiesRenderer extends HtmlRenderer<List<Specialty>> {

		@Override
		public String apply(List<Specialty> value) {
			return !value.isEmpty() ? super.apply(value) : "none";
		}
	}
}
