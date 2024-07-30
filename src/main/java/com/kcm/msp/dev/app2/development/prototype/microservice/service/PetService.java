package com.kcm.msp.dev.app2.development.prototype.microservice.service;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.CreatePetRequest;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.Pet;
import java.util.List;

public interface PetService {
  List<Pet> listPets(Integer limit);

  Pet showPetById(String id);

  Pet createPet(CreatePetRequest request);
}
