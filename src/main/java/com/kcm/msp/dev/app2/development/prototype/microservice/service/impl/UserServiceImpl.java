package com.kcm.msp.dev.app2.development.prototype.microservice.service.impl;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.User;
import com.kcm.msp.dev.app2.development.prototype.microservice.service.UserService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Override
  public List<User> listUsers(final Integer limit) {
    Objects.requireNonNull(limit, "Limit cannot be null");
    final List<User> users = List.of(new User(123L, "test"));
    return users.stream().limit(limit).collect(Collectors.toList());
  }
}
