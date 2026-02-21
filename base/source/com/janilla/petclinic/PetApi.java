package com.janilla.petclinic;

public interface PetApi {

	Pet create(Pet pet);

	Pet read(Long id, Integer depth);

	Pet update(Long id, Pet pet);
}
