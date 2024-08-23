package com.kcm.msp.dev.app2.development.prototype.microservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kcm.msp.dev.app2.development.prototype.microservice.controller.PrototypeController;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Tag("IntegrationTest")
@DisabledIf(expression = "#{environment['skip.integration.test'] == 'true'}")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
final class App2DevelopmentPrototypeMicroserviceApplicationTests {

  @LocalServerPort private int port;

  @Autowired private RestClient restClient;

  @Autowired private PrototypeController prototypeController;

  @Test
  void contextLoads() {
    assertNotNull(prototypeController, "The prototypeController should not be null");
  }

  @Test
  void testMainMethod() {
    App2DevelopmentPrototypeMicroserviceApplication.main(new String[] {""});
    assertTrue(true, "main method executed");
  }

  // TODO move these test to a different test class
  @Nested
  class TestActuatorEndpoints {

    @Test
    public void testHealthEndpoint() {
      final var response =
          restClient.get().uri(getBaseUrl("/actuator/health")).retrieve().toEntity(String.class);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "\"status\":\"UP\""));
    }

    @Test
    public void testInfoEndpoint() {
      final var response =
          restClient.get().uri(getBaseUrl("/actuator/info")).retrieve().toBodilessEntity();
      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testMetricsEndpoint() {
      final var response =
          restClient.get().uri(getBaseUrl("/actuator/metrics")).retrieve().toBodilessEntity();
      assertEquals(HttpStatus.OK, response.getStatusCode());
    }
  }

  @Nested
  class TestSecurityFilterChain {

    @Test
    public void accessingActuatorEndpointWithoutAuthShouldBeOk() {
      final var response =
          restClient.get().uri(getBaseUrl("/actuator/health")).retrieve().toBodilessEntity();
      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void accessingPetsEndpointWithoutAuthShouldBeOk() {
      final var response =
          restClient.get().uri(getBaseUrl("/pets/123")).retrieve().toBodilessEntity();
      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void accessingUsersEndpointWithoutAuthShouldFail() {
      final HttpClientErrorException exception =
          assertThrows(
              HttpClientErrorException.class,
              () -> {
                restClient.get().uri(getBaseUrl("/users")).retrieve().toBodilessEntity();
              });
      assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    @Disabled("This test is disabled until header origin test is fixed")
    void testCorsAllowedOrigin() {
      final var origin = "https://editor.swagger.io";
      var response =
          restClient
              .get()
              .uri(getBaseUrl("/users"))
              .accept(MediaType.APPLICATION_JSON)
              .headers(
                  header -> {
                    header.setBasicAuth("kannan", "kannan");
                    header.setOrigin(origin);
                  })
              .retrieve()
              .toEntity(new ParameterizedTypeReference<>() {});
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(origin, response.getHeaders().getAccessControlAllowOrigin()); // TODO fix this
    }
  }

  private String getBaseUrl(String endpoint) {
    return "http://localhost:" + port + endpoint;
  }
}
