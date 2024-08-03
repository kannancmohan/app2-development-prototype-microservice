package com.kcm.msp.dev.app2.development.prototype.microservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kcm.msp.dev.app2.development.prototype.microservice.controller.PrototypeController;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.DisabledIf;
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
        restClient.get().uri(getBaseUrl("/actuator/info")).retrieve().toEntity(String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testMetricsEndpoint() {
    final var response =
        restClient.get().uri(getBaseUrl("/actuator/metrics")).retrieve().toEntity(String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  private String getBaseUrl(String endpoint) {
    return "http://localhost:" + port + endpoint;
  }
}
