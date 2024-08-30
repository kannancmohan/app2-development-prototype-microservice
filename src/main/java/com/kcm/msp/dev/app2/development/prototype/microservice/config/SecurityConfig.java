package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.*;

import com.kcm.msp.dev.app2.development.prototype.microservice.properties.CorsProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
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
                conf.jwt(Customizer.withDefaults())
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

  //  private JwtAuthenticationConverter jwtAuthenticationConverter() {
  //    JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
  //    /*    converter.setAuthoritiesClaimName("roles");
  //    converter.setAuthorityPrefix("ROLE_");*/
  //
  //    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
  //    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);
  //    return jwtAuthenticationConverter;
  //  }

  // TODO remove this
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(
        User.withUsername("kannan").password("{noop}kannan").authorities("ROLE_USER").build());
  }
}
