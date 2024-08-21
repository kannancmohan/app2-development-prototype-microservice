package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocOpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .version("1.0.0")) // TODO find a way to extract the version id from openapi spec.
        .components(
            new Components()
                .addSecuritySchemes(
                    "basic_auth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
  }
}
