package com.janilla.petclinic.frontend;

import java.net.URI;
import java.util.List;

import com.janilla.persistence.ListPortion;
import com.janilla.petclinic.Owner;
import com.janilla.petclinic.Pet;
import com.janilla.web.Render;

@Render(template = "ownersList.html")
public record OwnersList(List<Result> results, Paginator paginator) {

	public static OwnersList of(ListPortion<Owner> owners, int page) {
		return new OwnersList(owners.elements().stream().map(x -> new Result(x, x.pets())).toList(),
				new Paginator(page - 1, (int) Math.ceilDiv(owners.totalSize(), 5), URI.create("/owners")));
	}

	@Render(template = "result")
	public record Result(Owner owner, @Render(delimiter = ", ") List<@Render(template = "pet") Pet> pets) {
	}
}