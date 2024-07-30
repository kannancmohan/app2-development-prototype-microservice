package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfig {

  @Bean
  public RestTemplate restTemplate(final RestTemplateBuilder builder) {
    // Add additional configuration here
    return builder.build();
  }
}
