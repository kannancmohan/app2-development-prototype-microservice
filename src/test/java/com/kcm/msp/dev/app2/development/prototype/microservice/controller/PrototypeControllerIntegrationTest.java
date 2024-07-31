package com.kcm.msp.dev.app2.development.prototype.microservice.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import com.kcm.msp.dev.app2.development.prototype.microservice.exception.ItemNotFoundException;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.CreatePetRequest;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.Error;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.Pet;
import com.kcm.msp.dev.app2.development.prototype.microservice.service.PetService;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.web.util.UriComponentsBuilder;

@Tag("IntegrationTest")
@DisabledIf(expression = "#{environment['skip.integration.test'] == 'true'}")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
final class PrototypeControllerIntegrationTest {

  private static final String TEST_URL = "http://localhost:";

  @LocalServerPort int port;

  @Autowired TestRestTemplate restTemplate;

  // comment this and its references to do a real integration test
  @MockBean private PetService petService;

  @Nested
  class TestGetPets {

    @Test
    @DisplayName("GET /pets should return list of pets")
    void petsShouldReturnListOfPets() throws Exception {
      when(petService.listPets(any())).thenReturn(List.of(getPetInstance()));
      final URI uri = UriComponentsBuilder.fromHttpUrl(TEST_URL + port + "/pets").build().toUri();
      final ResponseEntity<List<Pet>> responseEntity =
          restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
      assertNotNull(responseEntity);
      assertNotNull(responseEntity.getBody());
      assertAll(
          () -> assertEquals(OK, responseEntity.getStatusCode()),
          () -> assertTrue(responseEntity.hasBody()),
          () -> assertFalse(responseEntity.getBody().isEmpty()));
    }

    @Test
    @DisplayName("GET /pets with invalid email should return BAD_REQUEST")
    void petsShouldReturnBadRequestForInvalidEmail() {
      // TODO
    }

    @Test
    @DisplayName("GET /pets with invalid date should return BAD_REQUEST")
    void petsShouldReturnBadRequestForInvalidDate() {
      final URI uri =
          UriComponentsBuilder.fromHttpUrl(TEST_URL + port + "/pets")
              .queryParam("date-of-birth", "invalid_date")
              .build()
              .toUri();
      final ResponseEntity<String> responseEntity =
          restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
      assertNotNull(responseEntity);
      assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("GET /pets should return INTERNAL_SERVER_ERROR")
    void petsShouldReturnError() throws Exception {
      when(petService.listPets(any())).thenThrow(new RuntimeException("some error occurred"));
      final URI uri = UriComponentsBuilder.fromHttpUrl(TEST_URL + port + "/pets").build().toUri();
      final ResponseEntity<Error> responseEntity =
          restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
      assertNotNull(responseEntity);
      assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }
  }

  @Nested
  class TestGetShowPetById {

    @Test
    @DisplayName("GET /pets/{petId} should return a pet")
    void showPetByIdShouldReturnPet() throws Exception {
      when(petService.showPetById(any())).thenReturn(getPetInstance());
      final URI uri =
          UriComponentsBuilder.fromHttpUrl(TEST_URL + port + "/pets/123").build().toUri();
      final ResponseEntity<Pet> responseEntity =
          restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
      assertNotNull(responseEntity);
      assertAll(
          () -> assertEquals(OK, responseEntity.getStatusCode()),
          () -> assertNotNull(responseEntity.getBody()));
    }

    @Test
    @DisplayName("GET /pets/{petId} should return NOT_FOUND")
    void showPetByIdShouldReturnException() throws Exception {
      when(petService.showPetById(any())).thenThrow(new ItemNotFoundException("item not found"));
      final URI uri =
          UriComponentsBuilder.fromHttpUrl(TEST_URL + port + "/pets/123").build().toUri();
      final ResponseEntity<Error> responseEntity =
          restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
      assertNotNull(responseEntity);
      assertAll(
          () -> assertEquals(NOT_FOUND, responseEntity.getStatusCode()),
          () -> assertNotNull(responseEntity.getBody()));
    }
  }

  @Nested
  class TestCreatePets {

    @Test
    @DisplayName("POST /pets should return a pet")
    void petsShouldReturnAPet() throws Exception {
      when(petService.createPet(any())).thenReturn(getPetInstance());
      final URI uri = UriComponentsBuilder.fromHttpUrl(TEST_URL + port + "/pets").build().toUri();
      final CreatePetRequest createPetRequest =
          new CreatePetRequest()
              .name("petName")
              .tag("petTag")
              .dateOfBirth(LocalDate.now())
              .ownerEmail("test@test.com");
      HttpEntity<CreatePetRequest> request = new HttpEntity<>(createPetRequest, new HttpHeaders());
      final ResponseEntity<Pet> responseEntity =
          restTemplate.postForEntity(uri, request, Pet.class);
      assertNotNull(responseEntity);
      assertNotNull(responseEntity.getBody());
      assertAll(
          () -> assertEquals(OK, responseEntity.getStatusCode()),
          () -> assertTrue(responseEntity.hasBody()),
          () -> assertEquals("petName", responseEntity.getBody().getName()));
    }

    @Test
    @DisplayName("POST /pets with invalid email should return BAD_REQUEST")
    void petsShouldReturnBadRequestForInvalidEmail() {
      // TODO
    }
  }

  private Pet getPetInstance() {
    return new Pet().id(123L).name("petName").tag("petTag");
  }
}
