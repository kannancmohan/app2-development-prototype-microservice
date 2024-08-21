package com.kcm.msp.dev.app2.development.prototype.microservice.controller;

import static org.springframework.http.HttpStatus.OK;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.api.UserApi;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.User;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController implements UserApi {

  @Override
  public ResponseEntity<List<User>> listUsers() {
    final List<User> users = List.of(new User(123L, "test"));
    return ResponseEntity.status(OK).body(users);
  }
}
