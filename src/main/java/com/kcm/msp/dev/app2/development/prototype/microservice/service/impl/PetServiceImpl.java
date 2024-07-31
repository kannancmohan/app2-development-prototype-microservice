package com.kcm.msp.dev.app2.development.prototype.microservice.service.impl;

import com.kcm.msp.dev.app2.development.prototype.microservice.exception.ItemNotFoundException;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.CreatePetRequest;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.Pet;
import com.kcm.msp.dev.app2.development.prototype.microservice.service.PetService;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PetServiceImpl implements PetService {

  public static final Random RANDOM = new Random();
  private static final List<Pet> SAMPLE_PETS =
      Stream.iterate(1, n -> n + 1)
          .limit(21)
          .map(
              value ->
                  new Pet()
                      .id(Long.valueOf(value))
                      .name("petName" + value)
                      .tag("petTag" + value)
                      .dateOfBirth(LocalDate.now())
                      .ownerEmail("test" + value + "@email.com"))
          .collect(Collectors.toList());

  @Override
  public List<Pet> listPets(final Integer limit) {
    Objects.requireNonNull(limit, "Limit cannot be null");
    return SAMPLE_PETS.stream().limit(limit).collect(Collectors.toList());
  }

  @Override
  public Pet showPetById(String id) {
    if (StringUtils.isBlank(id)) {
      throw new ItemNotFoundException("Item not found");
    }
    return new Pet().id(123L).name("petName").tag("petTag");
  }

  @Override
  public Pet createPet(final CreatePetRequest request) {
    Objects.requireNonNull(request, "CreatePetRequest cannot be null");
    final Pet pet =
        new Pet()
            .id(RANDOM.nextLong())
            .name(request.getName())
            .tag(request.getTag())
            .dateOfBirth(request.getDateOfBirth())
            .ownerEmail(request.getOwnerEmail());
    SAMPLE_PETS.add(pet);
    return pet;
  }
}
