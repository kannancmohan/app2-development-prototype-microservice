package com.kcm.msp.dev.app2.development.prototype.microservice;

import com.kcm.msp.dev.app2.development.prototype.microservice.properties.CorsProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CorsProperty.class)
public class App2DevelopmentPrototypeMicroserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(App2DevelopmentPrototypeMicroserviceApplication.class, args);
  }
}
