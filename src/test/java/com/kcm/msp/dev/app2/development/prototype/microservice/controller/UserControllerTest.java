package com.kcm.msp.dev.app2.development.prototype.microservice.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.User;
import com.kcm.msp.dev.app2.development.prototype.microservice.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@Tag("UnitTest")
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @MockBean private UserService userService;

  @Autowired private MockMvc mockMvc;

  @Nested
  class TestListUsers {

    @BeforeEach
    void beforeEach() {
      when(userService.listUsers(anyInt())).thenReturn(List.of(new User(123L, "test")));
    }

    @Test
    @WithMockUser(username = "user")
    void testListUsersWithUser() throws Exception {
      mockMvc.perform(get("/users")).andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testListUsersWithoutUser() throws Exception {
      mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
    }
  }
}
