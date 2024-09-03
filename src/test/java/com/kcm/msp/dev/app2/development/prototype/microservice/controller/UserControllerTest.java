package com.kcm.msp.dev.app2.development.prototype.microservice.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcm.msp.dev.app2.development.prototype.microservice.config.SecurityConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@Tag("UnitTest")
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

  @MockBean private UserService userService;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Nested
  class TestShowUserById {

    @BeforeEach
    void beforeEach() {
      when(userService.showUserById(anyString())).thenReturn(getUserInstance());
    }

    @Test
    @WithMockUser(roles = {"USER_ROLE"})
    void testShowUserWithUser() throws Exception {
      mockMvc.perform(get("/users/111")).andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testShowUserWithoutUser() throws Exception {
      mockMvc.perform(get("/users/111")).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class TestListUsers {

    @BeforeEach
    void beforeEach() {
      when(userService.listUsers(anyInt())).thenReturn(List.of(getUserInstance()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN_ROLE"})
    void testListUsersWithUser() throws Exception {
      mockMvc.perform(get("/admin/users")).andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testListUsersWithoutUser() throws Exception {
      mockMvc.perform(get("/admin/users")).andExpect(status().isUnauthorized());
    }
  }

  private User getUserInstance() {
    return new User().id(123L).name("petName").email("test@test.com");
  }
}
