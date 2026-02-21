package com.janilla.petclinic;

import com.janilla.persistence.ListPortion;

public interface VetApi {

	ListPortion<Vet> read(Integer depth, Integer skip, Integer limit);
}
