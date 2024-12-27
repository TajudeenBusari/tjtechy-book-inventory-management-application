package com.tjtechy.tjtechyinventorymanagementsept2024.author.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto.AuthorDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Date;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for Author API endpoints")
@Tag("integration")
/*
 * this will override the active profile in application.yml file.
 * Irrespective of the active profile, the test will only run using the
 * h2-database
 *
 */
@ActiveProfiles(value = "h2-database")
public class AuthorControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc; //THIS IS THE ONLY MOCK BCOS WE HAVE TO SIMULATE AN HTTP REQUEST

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Autowired
  ObjectMapper objectMapper;

  private String token;

  @BeforeEach
  void setUp() throws Exception {
    ResultActions resultActions = this.mockMvc
            .perform(post(this.baseUrl + "/users/login").with(httpBasic("ben", "123456")));

    MvcResult mvcResult = resultActions.andDo(print()).andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();

    JSONObject json = new JSONObject(contentAsString);
    this.token = "Bearer " + json.getJSONObject("data").getString("token");
  }

  @Test
  @DisplayName("Check Add Author (POST)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testAddAuthorSuccess() throws Exception {
    var author = new AuthorDto(
            null,
            "first-name1",
            "last-name1",
            "testemail1@email.com",
            "biogrpahy1",
            null
    );

    var json = this.objectMapper.writeValueAsString(author);
    this.mockMvc.perform(post(baseUrl + "/authors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Add Success"))
            .andExpect(jsonPath("$.data.authorId").isNotEmpty())
            .andExpect(jsonPath("$.data.firstName").value("first-name1"))
            .andExpect(jsonPath("$.data.lastName").value("last-name1"))
            .andExpect(jsonPath("$.data.email").value("testemail1@email.com"))
            .andExpect(jsonPath("$.data.biography").value("biogrpahy1"))
            .andExpect(jsonPath("$.data.numberOfBooks").isNotEmpty());

  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindAllAuthorsSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/authors")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Find All Success"));

  }

  @Test
  @DisplayName("Check findAuthorByIdSuccess (GET)")
  void testFindAuthorByIdSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/authors/2")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Find One Success"))
            .andExpect(jsonPath("$.data.authorId").value("2"));

  }

  @Test
  @DisplayName("Check findAuthorByIdFailure (GET)")
  void testFindAuthorByIdNotSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/authors/8")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
            .andExpect(jsonPath("$.message").value("Could not find author with Id 8"))
            .andExpect(jsonPath("$.data").isEmpty());

  }

  @Test
  @DisplayName("Check updateBook with valid input (put)")
  void testUpdateAuthorSuccess() throws Exception {
    var authorDto = new AuthorDto(
            null,
            "Taju update",
            "Gani",
            "taju@gmail.com",
            "author 1 biography",
            2
    );

    var json = this.objectMapper.writeValueAsString(authorDto);
    this.mockMvc.perform(put(baseUrl + "/authors/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Update Success"))
            .andExpect(jsonPath("$.data.authorId").isNotEmpty())
            .andExpect(jsonPath("$.data.firstName").value("Taju update"))
            .andExpect(jsonPath("$.data.numberOfBooks").isNotEmpty());
  }


  @Test
  @DisplayName("Check updateAuthor with invalid authorId (put)")
  void testUpdateAuthorFailureWithNonExistingId() throws Exception {

    var authorDto = new AuthorDto(
            null,
            "Taju update",
            "Gani",
            "taju@gmail.com",
            "author 1 biography",
            2
    );

    var json = this.objectMapper.writeValueAsString(authorDto);
    this.mockMvc.perform(put(baseUrl + "/authors/7")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
            .andExpect(jsonPath("$.message").value("Could not find author with Id 7"))
            .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check deleteAuthor with valid authorId (Delete)")
  void testDeleteAuthorSuccess() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/authors/2")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Delete Success"));
    this.mockMvc.perform(get(this.baseUrl + "/authors/2")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
            .andExpect(jsonPath("$.message").value("Could not find author with Id 2"))
            .andExpect(jsonPath("$.data").isEmpty());

  }

  @Test
  @DisplayName("Check deleteAuthor with invalid authorId (Delete)")
  void testDeleteAuthorFailureWithNonExistingId() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/authors/4")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
            .andExpect(jsonPath("$.message").value("Could not find author with Id 4"))
            .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check assign book to author with valid Ids (Put)")
  void testAssignAuthorSuccess() throws Exception{

    Date date = new Date(1726680002000L);
    var book = new BookDto(
            null,
            "title1",
            date,
            "publisher1",
            "genre1",
            "edition1",
            "finnish",
            1000,
            "description1",
            100.0,
            "10",
            null
    );
    var json = this.objectMapper.writeValueAsString(book);
    var postResult = this.mockMvc.perform(post(this.baseUrl + "/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Add Success"))
            .andExpect(jsonPath("$.data.ISBN").isNotEmpty())
            .andReturn();

    // Extract the bookId (UUID) from the POST response
    var responseContent = postResult.getResponse().getContentAsString();
    var response = new JSONObject(responseContent);
    var bookId = response.getJSONObject("data").getString("ISBN");

    this.mockMvc.perform(put(this.baseUrl + "/authors/3/books/{bookId}", bookId)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, this.token))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
            .andExpect(jsonPath("$.message").value("Book Assignment Success"))
            .andExpect(jsonPath("$.data").isEmpty());

  }
}
