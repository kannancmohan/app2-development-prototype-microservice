package com.kcm.msp.dev.app2.development.prototype.microservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcm.msp.dev.app2.development.prototype.microservice.server.models.CreateUserRequest;
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

  @Autowired private ObjectMapper objectMapper;

  @Nested
  class TestListUsers {

    @BeforeEach
    void beforeEach() {
      when(userService.listUsers(anyInt())).thenReturn(List.of(getUserInstance()));
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

  @Nested
  class TestCreateUsers {

    @Test
    @WithMockUser(username = "user")
    void testCreateUsersWithUser() throws Exception {
      final var user = "test";
      final var email = "test@test.com";
      when(userService.createUser(any())).thenReturn(new User().id(123L).name(user).email(email));
      final var jsonString =
          objectMapper.writeValueAsString(new CreateUserRequest().name(user).email(email));
      mockMvc
          .perform(post("/users").contentType(APPLICATION_JSON).content(jsonString).with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value(user))
          .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @WithAnonymousUser
    void testCreateUsersWithoutUser() throws Exception {
      final var jsonString =
          objectMapper.writeValueAsString(
              new CreateUserRequest().name("test").email("test@test.com"));
      mockMvc
          .perform(post("/users").contentType(APPLICATION_JSON).content(jsonString).with(csrf()))
          .andExpect(status().isUnauthorized());
    }
  }

  private User getUserInstance() {
    return new User().id(123L).name("petName").email("test@test.com");
  }
}
