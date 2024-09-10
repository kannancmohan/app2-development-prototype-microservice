package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

@Slf4j
@Configuration
public class OpenTelemetryTracingConfig {

  @Bean
  @ConditionalOnProperty("management.tracing.enabled")
  public ObservationRegistryCustomizer<ObservationRegistry> skipActuatorEndpointsFromObservation() {
    log.info("Using custom skipActuatorEndpointsFromObservation bean to skip actuator endpoints");
    final var pathMatcher = new AntPathMatcher("/");
    return registry ->
        registry.observationConfig().observationPredicate(observationPredicate(pathMatcher));
  }

  private ObservationPredicate observationPredicate(final PathMatcher pathMatcher) {
    return (observationName, context) -> {
      if (context instanceof ServerRequestObservationContext observationContext) {
        return !pathMatcher.match("/actuator/**", observationContext.getCarrier().getRequestURI());
      } else {
        return false;
      }
    };
  }
}
