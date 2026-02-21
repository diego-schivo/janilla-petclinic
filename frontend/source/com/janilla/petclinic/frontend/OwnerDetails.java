package com.janilla.petclinic.frontend;

import java.util.List;

import com.janilla.petclinic.Owner;
import com.janilla.petclinic.Pet;
import com.janilla.petclinic.Visit;
import com.janilla.web.Render;

@Render(template = "ownerDetails.html")
public record OwnerDetails(Owner owner, List<Pet2> pets) {

	public static OwnerDetails of(Owner owner) {
		return new OwnerDetails(owner, owner.pets().stream().map(x -> new Pet2(x, x.visits())).toList());
	}

	@Render(template = "pet")
	public record Pet2(Pet pet, List<@Render(template = "visit") Visit> visits) {
	}
}