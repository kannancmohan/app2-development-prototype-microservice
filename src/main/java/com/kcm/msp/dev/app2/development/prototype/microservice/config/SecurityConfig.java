package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.*;

import com.kcm.msp.dev.app2.development.prototype.microservice.properties.CorsProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity // comment this "To disable springboot-security"
@EnableMethodSecurity // added to make use of @PreAuthorize and @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

  private static final String[] WHITELISTED_API_ENDPOINTS = {
    "/pets/**", "/actuator/**", "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/**"
  };

  private final CorsProperty corsProperty;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless APIs(like REST APIs)
        .cors(c -> c.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(WHITELISTED_API_ENDPOINTS)
                    .permitAll()
                    .anyRequest() // authenticate remaining requests
                    .authenticated())
        .oauth2ResourceServer(
            conf ->
                conf.jwt(
                        jwtConf -> jwtConf.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                    .authenticationEntryPoint(customAuthenticationEntryPoint()))
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(
            session ->
                session.sessionCreationPolicy(
                    STATELESS)); // Ensure stateless session for rest api's
    return http.build();
  }

  @Bean
  public AuthenticationEntryPoint customAuthenticationEntryPoint() {
    // replace WWW-Authenticate header content
    return (request, response, authException) -> {
      response.addHeader(WWW_AUTHENTICATE, "Bearer realm=\"Restricted Content\"");
      response.sendError(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase());
    };
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProperty.getAllowedOrigins());
    configuration.setAllowedMethods(corsProperty.getAllowedMethods());
    configuration.setAllowedHeaders(corsProperty.getAllowedHeaders());
    configuration.setAllowCredentials(true);
    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    final var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::generateKeycloakGrantedAuthorities);
    return converter;
  }

  @SuppressWarnings("unchecked")
  private static Collection<GrantedAuthority> generateKeycloakGrantedAuthorities(final Jwt jwt) {
    final var authorities = new ArrayList<GrantedAuthority>();
    if (jwt != null && jwt.hasClaim("realm_access")) {
      final var realmAccess = jwt.getClaimAsMap("realm_access");
      final var roles =
          ((List<String>) realmAccess.get("roles"))
              .stream()
                  .map(roleName -> "ROLE_" + roleName)
                  .map(SimpleGrantedAuthority::new)
                  .toList();
      authorities.addAll(roles);
    }
    if (jwt != null && jwt.hasClaim("scope")) {
      final var scopes = Arrays.asList(jwt.getClaimAsString("scope").split(" "));
      authorities.addAll(
          scopes.stream()
              .map(scopeName -> "SCOPE_" + scopeName)
              .map(SimpleGrantedAuthority::new)
              .toList());
    }
    return authorities;
  }

  // TODO remove this
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(
        User.withUsername("kannan").password("{noop}kannan").authorities("ROLE_USER").build());
  }
}
