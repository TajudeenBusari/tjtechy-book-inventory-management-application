package com.tjtechy.tjtechyinventorymanagementsept2024.user.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.controller.BookController;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("Integration tests for Library User API endpoints")
@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
/*
 * this will override the active profile in application.yml file.
 * Irrespective of the active profile, the test will only run using the
 * h2-database
 *
 */
@ActiveProfiles(value = "application-h2-database")
public class LibraryUserControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc; //THIS IS THE ONLY MOCK BCOS WE HAVE TO SIMULATE AN HTTP REQUEST

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Autowired
  ObjectMapper objectMapper;

  private String token;


  @BeforeEach
  void setUp() throws Exception {
    // User ben has all permissions.
    ResultActions resultActions = this.mockMvc
            .perform(post(this.baseUrl + "/users/login").with(httpBasic("ben", "123456")));

    MvcResult mvcResult = resultActions.andDo(print()).andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();

    JSONObject json = new JSONObject(contentAsString);
    this.token = "Bearer " + json.getJSONObject("data").getString("token");
  }

  @Test
  @DisplayName("Check findAllLibrarysers (GET)")
  void testFindAllLibraryUsersSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/users")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Find All Success"));
  }


  @Test
  @DisplayName("Check findLibraryUser with valid Id (GET): User with ROLE_Admin Accessing Any LibraryUser's Info")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindLibraryUserWithAdminAccessingAnyLibraryUserSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/users/2")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Find One Success"))
            .andExpect(jsonPath("$.data.userId").value(2));
  }

  @Test
  @DisplayName("Check findLibraryUser with valid Id (GET): User with ROLE_user Accessing Another LibraryUser's Info")
  void testFindLibraryUserWithUserAccessingAnyLibraryUserSuccess() throws Exception {

    var resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login")
            .with(httpBasic("kim", "654321")));
    var mvcResult = resultActions.andDo(print()).andReturn();
    var conetntString = mvcResult.getResponse().getContentAsString();
    JSONObject json = new JSONObject(conetntString);
    String kimToken = "Bearer " + json.getJSONObject("data").getString("token");

    this.mockMvc.perform(get(this.baseUrl + "/users/2")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, kimToken))
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
            .andExpect(jsonPath("$.message").value("No permission"))
            .andExpect(jsonPath("$.data").value("Access Denied"));
  }

  @Test
  @DisplayName("Check add LibraryUser with valid input (POST)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testAddLibraryUserSuccess() throws Exception {
    var libraryUser = new LibraryUser();
    libraryUser.setUserName("zeenat");
    libraryUser.setPassword("98765");
    libraryUser.setRoles("user");
    libraryUser.setEnabled(true);

    var json = this.objectMapper.writeValueAsString(libraryUser);

    this.mockMvc.perform(post(this.baseUrl + "/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json).accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Add Success"))
            .andExpect(jsonPath("$.data.userId").isNotEmpty())
            .andExpect(jsonPath("$.data.userName").value("zeenat"))
            .andExpect(jsonPath("$.data.enabled").value(true))
            .andExpect(jsonPath("$.data.roles").value("user"));
  }

  /***
   * for now it is only Admin that can update or delete user
   * @throws Exception
   */

  @Test
  @DisplayName("Check deleteLibraryUser with valid input (DELETE)")
  void testDeleteLibraryUserSuccess() throws Exception {
    //only admin right can delete
    this.mockMvc.perform(delete(this.baseUrl + "/users/3")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Delete Success"))
            .andExpect(jsonPath("$.data").isEmpty());
    this.mockMvc.perform(get(this.baseUrl + "/users/3").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
            .andExpect(jsonPath("$.message").value("Could not find library user with Id 3"))
            .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check deleteLibraryUser with insufficient permission (DELETE)")
  void testDeleteUserNoAccessAsRoleUser() throws Exception {
    //kim is a normal user, he doesn't have that right
    var resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login")
            .with(httpBasic("kim", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    JSONObject json = new JSONObject(contentAsString);
    String kimToken = "Bearer " + json.getJSONObject("data").getString("token");

    this.mockMvc.perform(delete(this.baseUrl + "/users/3")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, kimToken)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
            .andExpect(jsonPath("$.message").value("No permission"));
    }


  /***
   * Not yet implemented in the controller
    * @throws Exception
   */
}
