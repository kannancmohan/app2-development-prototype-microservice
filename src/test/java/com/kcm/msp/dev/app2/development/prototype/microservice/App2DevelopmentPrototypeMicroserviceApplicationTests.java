package com.kcm.msp.dev.app2.development.prototype.microservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kcm.msp.dev.app2.development.prototype.microservice.controller.PrototypeController;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.DisabledIf;

@Tag("IntegrationTest")
@DisabledIf(expression = "#{environment['skip.integration.test'] == 'true'}")
@SpringBootTest
final class App2DevelopmentPrototypeMicroserviceApplicationTests {
  @Autowired private PrototypeController prototypeController;

  @Test
  void contextLoads() {
    assertNotNull(prototypeController, "The prototypeController should not be null");
  }

  @Test
  void testMainMethod() {
    App2DevelopmentPrototypeMicroserviceApplication.main(new String[] {""});
    assertTrue(true, "main method executed");
  }
}
