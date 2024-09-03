package com.kcm.msp.dev.app2.development.prototype.microservice.controller;

import static org.springframework.http.HttpStatus.OK;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.api.UserApi;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.User;
import com.kcm.msp.dev.app2.development.prototype.microservice.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  public ResponseEntity<User> showUserById(final String userId) {
    var user = userService.showUserById(userId);
    return ResponseEntity.status(OK).body(user);
  }

  @Override
  public ResponseEntity<List<User>> listUsers(final Integer limit) {
    final List<User> users = userService.listUsers(limit);
    return ResponseEntity.status(OK).body(users);
  }
}
