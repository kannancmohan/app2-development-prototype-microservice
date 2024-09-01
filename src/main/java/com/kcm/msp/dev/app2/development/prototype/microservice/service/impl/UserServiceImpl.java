package com.kcm.msp.dev.app2.development.prototype.microservice.service.impl;

import com.kcm.msp.dev.app2.development.prototype.microservice.exception.ItemNotFoundException;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.CreateUserRequest;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.User;
import com.kcm.msp.dev.app2.development.prototype.microservice.service.UserService;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  public static final Random RANDOM = new Random();
  private static final List<User> SAMPLE_USERS =
      Stream.iterate(1, n -> n + 1)
          .limit(21)
          .map(
              value ->
                  new User()
                      .id(Long.valueOf(value))
                      .name("name" + value)
                      .email("test" + value + "@email.com"))
          .collect(Collectors.toList());

  @Override
  public User showUserById(final String id) {
    if (StringUtils.isBlank(id)) {
      throw new ItemNotFoundException("Item not found");
    }
    return new User().id(100L).name("test@user.com").name("test@user.com");
  }

  @Override
  public List<User> listUsers(final Integer limit) {
    Objects.requireNonNull(limit, "Limit cannot be null");
    return SAMPLE_USERS.stream().limit(limit).collect(Collectors.toList());
  }

  @Override
  public User createUser(final CreateUserRequest request) {
    Objects.requireNonNull(request, "CreateUserRequest cannot be null");
    final User user =
        new User().id(RANDOM.nextLong()).name(request.getName()).email(request.getEmail());
    SAMPLE_USERS.add(user);
    return user;
  }
}
