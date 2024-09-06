package com.kcm.msp.dev.app2.development.prototype.microservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;

import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.web.client.RestClient;

@Tag("IntegrationTest")
@DisabledIf(expression = "#{environment['skip.integration.test'] == 'true'}")
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureObservability
public class ObservabilityIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private RestClient restClient;

  @Autowired private TestObservationRegistry registry;

  @Profile("test")
  @TestConfiguration
  static class TestObservabilityConfig {
    @Bean
    public TestObservationRegistry testObservationRegistry() {
      return TestObservationRegistry.create();
    }
  }

  @Nested
  class TestObservabilityMetrics {

    @Test
    void incomingHttpRequestShouldInvokeHttpObservation() {
      final var response =
          restClient.get().uri(getBaseUrl("/actuator/prometheus")).retrieve().toBodilessEntity();
      assertEquals(OK, response.getStatusCode());
      // accessing above request should invoke http.server.requests observation
      TestObservationRegistryAssert.assertThat(registry)
          .hasObservationWithNameEqualTo("http.server.requests")
          .that()
          .hasBeenStarted()
          .hasLowCardinalityKeyValue("outcome", "SUCCESS")
          .hasBeenStopped();
    }
  }

  private String getBaseUrl(final String endpoint) {
    return "http://localhost:" + port + endpoint;
  }
}
