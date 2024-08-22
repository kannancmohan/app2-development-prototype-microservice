package com.kcm.msp.dev.app2.development.prototype.microservice.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.api.PrototypeApi;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.CreatePetRequest;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.Pet;
import com.kcm.msp.dev.app2.development.prototype.microservice.service.PetService;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PrototypeController implements PrototypeApi {

  private final PetService petService;

  @Override
  public ResponseEntity<List<Pet>> listPets(
      final Integer limit, final String ownerEmail, final LocalDate dateOfBirth) {
    final List<Pet> pets = petService.listPets(limit);
    return ResponseEntity.status(OK).body(pets);
  }

  @Override
  public ResponseEntity<Pet> showPetById(final String id) {
    final Pet pet = petService.showPetById(id);
    return ResponseEntity.status(OK).body(pet);
  }

  @Override
  public ResponseEntity<Pet> createPets(final CreatePetRequest request) {
    final Pet pet = petService.createPet(request);
    return ResponseEntity.status(CREATED).body(pet);
  }
}
