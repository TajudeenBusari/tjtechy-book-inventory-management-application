package com.tjtechy.tjtechyinventorymanagementsept2024.book.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;


import java.util.Date;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * WE ARE CALLING DIRECTLY TO THE DB IN INTEGRATION TEST
 */

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for Book API endpoints")
@Tag("integration")
public class BookControllerIntegrationTest {

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
                .perform(post(this.baseUrl + "/users/login").with(httpBasic("tayo", "654321")));

        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();

        JSONObject json = new JSONObject(contentAsString);
        this.token = "Bearer " + json.getJSONObject("data").getString("token");


    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllBooksSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/books").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"));
                //.andExpect(jsonPath("$.data", Matchers.hasSize(15))); sort this out
    }

    @Test
    @DisplayName("Check addBook with valid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddBookSuccess() throws Exception {
        Date date = new Date(1726680002000L);
        var book = new BookDto(
                null,
                "book 2",
                date,
                "zeenat",
                "horror",
                "second edition",
                "yoruba",
                1000,
                "some description 2",
                1500.0,
                "10", null

        );


//        book.setTitle("Book Title");
//        book.setLanguage("English");
//        book.setPublicationDate(date);
//        book.setPages(100);
//        book.setPrice(100.0);
//        book.setEdition("first edition");
//        book.setQuantity("10");
//        book.setGenre("fiction");
//        book.setPublisher("young");
//        book.setDescription("description1");


        var json = this.objectMapper.writeValueAsString(book);
        this.mockMvc.perform(post(this.baseUrl + "/books")
                        .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.ISBN").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value(book.title()))
                .andExpect(jsonPath("$.data.language").value(book.language()))
                .andExpect(jsonPath("$.data.publicationDate").isNotEmpty())
                .andExpect(jsonPath("$.data.pages").value(book.pages()))
                .andExpect(jsonPath("$.data.price").value(book.price()))
                .andExpect(jsonPath("$.data.edition").value(book.edition()))
                .andExpect(jsonPath("$.data.Genre").value(book.Genre()))
                .andExpect(jsonPath("$.data.publisher").value(book.publisher()))
                .andExpect(jsonPath("$.data.description").value(book.description()))
                .andExpect(jsonPath("$.data.Quantity").value(book.Quantity()));
        this.mockMvc.perform(get(this.baseUrl + "/books").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"));
                //.andExpect(jsonPath("$.data", Matchers.hasSize(16))); sort this out
    }

    @Test
    @DisplayName("Check findBookByIdSuccess (GET)")
    void testFindBookByIdSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/books/0c1778a5-1bc8-43ea-bdeb-bc244f7f2298")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.ISBN").value("0c1778a5-1bc8-43ea-bdeb-bc244f7f2298"))
                .andExpect(jsonPath("$.data.title").value("book 2"))
                .andExpect(jsonPath("$.data.language").value("yoruba"))
                .andExpect(jsonPath("$.data.publicationDate").value("2024-09-18T17:20:02.000+00:00"))
                .andExpect(jsonPath("$.data.pages").value(1000))
                .andExpect(jsonPath("$.data.price").value(1500.0))
                .andExpect(jsonPath("$.data.edition").value("second edition"))
                .andExpect(jsonPath("$.data.Genre").value("horror"))
                .andExpect(jsonPath("$.data.publisher").value("zeenat"))
                .andExpect(jsonPath("$.data.description").value("some description 2"))
                .andExpect(jsonPath("$.data.Quantity").value("10"));
    }

    @Test
    @DisplayName("Check findBookById with non existent ISBN (GET)")
    void testFindBookByIdNotFound() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        this.mockMvc.perform(get(this.baseUrl + "/books/{nonExistingId}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find book with isbn " + nonExistingId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check addBook with invalid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddBookFailureWithInvalidInput() throws Exception {
        var book = new BookDto(
                null,
                "",
                null,
                "",
                "",
                "",
                "",
                0,
                "",
                100.0,
                "",
                null
        );
        var json = this.objectMapper.writeValueAsString(book);
        this.mockMvc.perform(post(this.baseUrl + "/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.BAD_REQUEST))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.title").value("title is required."))
                .andExpect(jsonPath("$.data.publisher").value("publisher is required."))
                .andExpect(jsonPath("$.data.Genre").value("genre is required."))
                .andExpect(jsonPath("$.data.language").value("language is required."))
                .andExpect(jsonPath("$.data.description").value("description is required."));
    }

    @Test
    @DisplayName("Check updateBook with valid input (put)")
    void testUpdateBookSuccess() throws Exception {
        Date date = new Date(1726680002000L);
        var book = new BookDto(
                null,
                "update book 2",
                date,
                "inayah",
                "fiction",
                "second edition",
                "yoruba",
                1000,
                "some description 2",
                1500.0,
                "10", null

        );

        var json = this.objectMapper.writeValueAsString(book);

        this.mockMvc.perform(put(this.baseUrl + "/books/0c1778a5-1bc8-43ea-bdeb-bc244f7f2298")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.ISBN").value("0c1778a5-1bc8-43ea-bdeb-bc244f7f2298"))
                .andExpect(jsonPath("$.data.publisher").value("inayah"))
                .andExpect(jsonPath("$.data.Genre").value("fiction"))
                .andExpect(jsonPath("$.data.title").value("update book 2"));

    }

    @Test
    @DisplayName("Check updateBook with non existing ISBN (put)")
    void testUpdateBookFailureWithNonExistingISBN() throws Exception {
        UUID nonExistingBookId = UUID.randomUUID();
        Date date = new Date(1726680002000L);
        var book = new BookDto(
                nonExistingBookId,
                "update some book",
                date,
                "update some publisher",
                "fiction",
                "second edition",
                "english",
                1000,
                "some description 2",
                1500.0,
                "10", null
        );

        var json = this.objectMapper.writeValueAsString(book);

        this.mockMvc.perform(put(this.baseUrl + "/books/{nonExistingBookId}", nonExistingBookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find book with isbn " + nonExistingBookId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteBook with valid input (Delete)")
    void testDeleteBookWithSuccess() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/books/3d1388ba-1a94-45ae-8531-47f8fd8d0d91")
                .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));
        this.mockMvc.perform(get(this.baseUrl + "/books/3d1388ba-1a94-45ae-8531-47f8fd8d0d91")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find book with isbn 3d1388ba-1a94-45ae-8531-47f8fd8d0d91"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

}
