package com.kcm.msp.dev.app2.development.prototype.microservice;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;

import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.BeforeEach;
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

  @Autowired private InMemorySpanExporter spanExporter;

  @Profile("test")
  @TestConfiguration
  static class TestObservabilityConfig {
    @Bean // added for metrics test
    public TestObservationRegistry testObservationRegistry() {
      return TestObservationRegistry.create();
    }

    @Bean // added for tracing test
    public SdkTracerProvider sdkTracerProvider(InMemorySpanExporter spanExporter) {
      return SdkTracerProvider.builder()
          .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
          .build();
    }

    @Bean //// added for tracing test
    public InMemorySpanExporter spanExporter() {
      return InMemorySpanExporter.create();
    }
  }

  @Nested
  class TestObservabilityMetrics {

    @Test
    void actuatorPrometheusEndpointShouldReturnMetrics() {
      final var response =
          restClient.get().uri(getBaseUrl("/actuator/prometheus")).retrieve().toBodilessEntity();
      assertEquals(OK, response.getStatusCode());
      // TODO check if response body has required prometheus metrics
    }

    @Test
    void incomingHttpRequestShouldInvokeHttpObservation() {
      final var response = restClient.get().uri(getBaseUrl("/pets")).retrieve().toBodilessEntity();
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

  @Nested
  class TestObservabilityTracing {
    @BeforeEach
    void beforeEach() {
      spanExporter.reset(); // Clear spans before each test
    }

    @Test
    void invokingHttpEndpointShouldGenerateSpanData() {
      final var endpoint = "/pets";
      final var response = restClient.get().uri(getBaseUrl(endpoint)).retrieve().toBodilessEntity();
      assertEquals(OK, response.getStatusCode());
      final var spans = spanExporter.getFinishedSpanItems();
      assertFalse(spans.isEmpty());
      final var span =
          spans.stream()
              .filter(spanData -> spanData.getName().contains(endpoint))
              .findFirst()
              .orElse(null);
      assertAll(
          () -> assertNotNull(span),
          () -> assertNotNull(span.getSpanId()),
          () -> assertNotNull(span.getTraceId()));
    }

    @Test
    void invokingExcludedHttpEndpointShouldNotGenerateSpanData() {
      final var endpoint = "/actuator/prometheus";
      final var response = restClient.get().uri(getBaseUrl(endpoint)).retrieve().toBodilessEntity();
      assertEquals(OK, response.getStatusCode());
      final var spans = spanExporter.getFinishedSpanItems();
      assertTrue(spans.isEmpty());
    }
  }

  private String getBaseUrl(final String endpoint) {
    return "http://localhost:" + port + endpoint;
  }
}
