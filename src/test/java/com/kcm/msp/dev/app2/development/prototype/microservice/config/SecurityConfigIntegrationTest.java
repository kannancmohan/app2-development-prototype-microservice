package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.kcm.msp.dev.app2.development.prototype.microservice.JWTUtil;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Tag("IntegrationTest")
@DisabledIf(expression = "#{environment['skip.integration.test'] == 'true'}")
@ActiveProfiles("test")
@AutoConfigureWireMock
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.main.allow-bean-definition-overriding=true",
      "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:${wiremock.server.port}/discovery/v2.0/keys"
    })
public class SecurityConfigIntegrationTest {
  private static final String TEST_USER = "testuser";
  private static final String TEST_USER_PWD = "testpassword";

  @LocalServerPort private int port;

  @Autowired private RestClient restClient;

  @Profile("test")
  @TestConfiguration
  static class TestUserDetailsServiceConfig {
    @Bean
    public UserDetailsService userDetailsService() {
      final var user =
          User.withUsername(TEST_USER)
              .password("{noop}" + TEST_USER_PWD) // {noop} indicates plain text password
              .roles("USER")
              .build();
      return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public WireMockConfiguration wireMockConfiguration() {
      return WireMockConfiguration.wireMockConfig()
          .extensions(new ResponseTemplateTransformer(false)) // Disable ResponseTemplateTransformer
          .dynamicPort();
    }
  }

  @Nested
  class TestAuthorizeHttpRequest {

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/actuator/info", "/actuator/metrics", "/pets/123"})
    void accessingWhiteListedEndpointWithoutAuthShouldBeOk(final String input) {
      final var response = restClient.get().uri(getBaseUrl(input)).retrieve().toBodilessEntity();
      assertEquals(OK, response.getStatusCode());
    }

    @Test
    void accessingUsersEndpointWithoutAuthShouldFail() {
      final HttpClientErrorException exception =
          assertThrows(
              HttpClientErrorException.class,
              () -> {
                restClient.get().uri(getBaseUrl("/users")).retrieve().toBodilessEntity();
              });
      assertEquals(UNAUTHORIZED, exception.getStatusCode());
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
              .accept(APPLICATION_JSON)
              .headers(
                  header -> {
                    header.setBasicAuth(TEST_USER, TEST_USER_PWD);
                    header.setOrigin(origin);
                  })
              .retrieve()
              .toEntity(new ParameterizedTypeReference<>() {});
      assertEquals(OK, response.getStatusCode());
      assertEquals(origin, response.getHeaders().getAccessControlAllowOrigin()); // TODO fix this
    }

    @Test
    @Disabled("TODO test cors with disallowed origin")
    void testCorsDisallowedOrigin() {}
  }

  @Nested
  class TestJwtBearerAuth {

    static final String JWKS = JWTUtil.createJWKS();

    @AfterEach
    void tearDown() {
      WireMock.reset(); // Reset WireMock after each test
    }

    @Test
    void accessingRestrictedEndpointWithValidTokenShouldBeOk() throws Exception {
      stubFor(
          get(urlEqualTo("/discovery/v2.0/keys"))
              .willReturn(
                  aResponse()
                      .withStatus(OK.value())
                      .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                      .withBody(JWKS)));

      final var jwtToken =
          JWTUtil.createJWT("test@test.com", Map.of("roles", List.of("USER_ROLE")));

      final var response =
          restClient
              .get()
              .uri(getBaseUrl("/users/111"))
              .headers(header -> header.setBearerAuth(jwtToken))
              .retrieve()
              .toEntity(
                  com.kcm.msp.dev.app2.development.prototype.microservice.server.models.User.class);
      assertEquals(OK, response.getStatusCode());
    }

    @Test
    void accessingRestrictedEndpointWithInValidTokenShouldFail() {
      stubFor(
          get(urlEqualTo("/discovery/v2.0/keys"))
              .willReturn(
                  aResponse()
                      .withStatus(OK.value())
                      .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                      .withBody(JWKS)));

      final var invalidJwtToken =
          "eyJraWQiOiJhMjgzMDI4MDQ4OTMwOTMwNzg1IiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdCIsInN1YiI6InRlc3RAdGVzdC5jb20iLCJleHAiOjE3MjUzNzYwMjksImlhdCI6MTcyNTM3NTk2OSwicm9sZXMiOlsiVVNFUl9ST0xFIl19.QbmFvJp2SedzLxnUuUJjUoVIx2m4FnYYlZS_f3gns390Wwx5_d90G-aQXqDG59j3AL9xJ5WUsUOIiTTYxTYSS15yHE2FCOP5XklnA2kX4TzKPsDJYRSRG1fgk-PnV5-IijkJ4pHgZzVPf1kRGyu_uXg-QwrlZ7u0aRAJTUAB5Bq5x0Q_41sRPPs_TjMr7lYiztCEqgxQLj3Wh01L_yQ6piZRyB2D_UC-cmTn4cYl1iGVgjqNGK0qfUqv8we4qmobl7S6zAHahkOVG_Ay2eRexH5R9L5dOpAkFY3VM1Uwrh2Pwg8pwog6o7ggFQ1k7ZQjFL1IppTMXq80ge8p95Fs9A";
      final var exception =
          assertThrows(
              HttpClientErrorException.class,
              () -> {
                restClient
                    .get()
                    .uri(getBaseUrl("/users/111"))
                    .headers(header -> header.setBearerAuth(invalidJwtToken))
                    .retrieve()
                    .toBodilessEntity();
              });
      assertEquals(UNAUTHORIZED, exception.getStatusCode());
    }
  }

  private String getBaseUrl(String endpoint) {
    return "http://localhost:" + port + endpoint;
  }
}
