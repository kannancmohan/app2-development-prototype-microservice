package com.kcm.msp.dev.app2.development.prototype.microservice.service;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.User;
import java.util.List;

public interface UserService {

  List<User> listUsers(Integer limit);
}
