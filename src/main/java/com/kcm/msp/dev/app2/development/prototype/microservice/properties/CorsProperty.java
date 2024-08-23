package com.kcm.msp.dev.app2.development.prototype.microservice.properties;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "cors")
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
@Getter
public class CorsProperty {
  private final List<String> allowedOrigins;
  private final List<String> allowedMethods;
  private final List<String> allowedHeaders;
}
