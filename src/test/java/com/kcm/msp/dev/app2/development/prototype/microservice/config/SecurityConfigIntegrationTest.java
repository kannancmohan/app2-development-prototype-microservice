package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Tag("IntegrationTest")
@DisabledIf(expression = "#{environment['skip.integration.test'] == 'true'}")
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = "spring.main.allow-bean-definition-overriding=true")
public class SecurityConfigIntegrationTest {
  private static final String TEST_USER = "testuser";
  private static final String TEST_USER_PWD = "testpassword";

  @LocalServerPort private int port;

  @Autowired private RestClient restClient;

  @TestConfiguration
  static class TestUserDetailsServiceConfig {
    @Bean
    @Primary // Mark this as the primary bean to override the existing UserDetailsService
    public UserDetailsService userDetailsService() {
      final var user =
          User.withUsername(TEST_USER)
              .password("{noop}" + TEST_USER_PWD) // {noop} indicates plain text password
              .roles("USER")
              .build();
      return new InMemoryUserDetailsManager(user);
    }
  }

  @Nested
  class TestAuthorizeHttpRequest {

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/actuator/info", "/actuator/metrics", "/pets/123"})
    void accessingWhiteListedEndpointWithoutAuthShouldBeOk(final String input) {
      final var response = restClient.get().uri(getBaseUrl(input)).retrieve().toBodilessEntity();
      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void accessingUsersEndpointWithoutAuthShouldFail() {
      final HttpClientErrorException exception =
          assertThrows(
              HttpClientErrorException.class,
              () -> {
                restClient.get().uri(getBaseUrl("/users")).retrieve().toBodilessEntity();
              });
      assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
  }

  @Nested
  class TestCors {

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
                    header.setBasicAuth(TEST_USER, TEST_USER_PWD);
                    header.setOrigin(origin);
                  })
              .retrieve()
              .toEntity(new ParameterizedTypeReference<>() {});
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(origin, response.getHeaders().getAccessControlAllowOrigin()); // TODO fix this
    }

    @Test
    @Disabled("TODO test cors with disallowed origin")
    void testCorsDisallowedOrigin() {}
  }

  private String getBaseUrl(String endpoint) {
    return "http://localhost:" + port + endpoint;
  }
}
