package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BeanConfig {

  @Bean
  public RestClient restClient(final RestClient.Builder builder) {
    // Add additional configuration here
    return builder.build();
  }
}
