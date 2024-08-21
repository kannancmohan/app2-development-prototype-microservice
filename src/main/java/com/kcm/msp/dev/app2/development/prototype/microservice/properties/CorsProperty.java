package com.kcm.msp.dev.app2.development.prototype.microservice.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "cors")
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
@Getter
public class CorsProperty {
  private final String allowedOrigins;
  private final String allowedMethods;
  private final String allowedHeaders;
}
