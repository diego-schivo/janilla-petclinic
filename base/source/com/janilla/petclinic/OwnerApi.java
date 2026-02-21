package com.janilla.petclinic;

import com.janilla.persistence.ListPortion;

public interface OwnerApi {

	Owner create(Owner owner);

	ListPortion<Owner> read(String lastName, Integer depth, Integer skip, Integer limit);

	Owner read(Long id, Integer depth);

	Owner update(Long id, Owner owner);
}
