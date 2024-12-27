package com.tjtechy.tjtechyinventorymanagementsept2024.author.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto.AuthorDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.repository.AuthorRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.repository.BookRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.repository.LibraryUserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.JsonPath;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Date;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Tag("Integration")
@ActiveProfiles(value = "postgre-database-test")
public class AuthorControllerTestContainerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibraryUserRepository userRepository; // Repository for LibraryUser

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // To encode passwords

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.0")
            .withDatabaseName("inventory-management-sept2024")
            .withUsername("postgres")
            .withPassword("postgres");


    /**
     *  .withDatabaseName("inventory-management-sept2024")
     *             .withUsername("postgres")
     *             .withPassword("postgres")
     *             .withExposedPorts(5432);
     * you can override the default configuration
     * of the container for example with builder pattern
     * by using a customized username, password, database name and port for the container
     * E.G:
     * static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.0")
     * .withDatabaseName("testdb")
     *  .withUsername("testuser")
     *   .withPassword("testpass");
     */

    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }
    //start container
    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    //stop container
    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    @BeforeEach
    void setUp() throws Exception {

        //this code execute the script to create database
            try (var connection = DriverManager.getConnection(postgreSQLContainer.getJdbcUrl(),
                    postgreSQLContainer.getUsername(),
                    postgreSQLContainer.getPassword())) {
                var schemaScript = Files.readString(Path.of("src/test/resources/schema.sql"));
                try (var statement = connection.createStatement()) {
                    statement.execute(schemaScript);
                }
            }

        /*
         *To address the issue, you can bypass the authentication requirement during
         * the initialization phase for your integration test. You need to populate
         * the database directly with the required user (ben with password 123456) before running your test,
         * without going through the controller.
         * Same thing is done to Author and Books-->the logic creates the Author and Books table
         * in the database.
         */

        //create the Admin user: ben with password: 123456
        if(userRepository.findByUserName("ben").isEmpty()) {
        var adminUser = new LibraryUser();
        adminUser.setUserName("ben");
        adminUser.setPassword(passwordEncoder.encode("123456"));
        adminUser.setRoles("Admin user");
        adminUser.setEnabled(true);
        userRepository.save(adminUser);
        }

        //create Author
        if(authorRepository.findByFirstNameAndLastName("Author1", "Author2").isEmpty()) {
        var author = new Author();
        author.setFirstName("Author1");
        author.setLastName("Author2");
        author.setEmail("author1@gmail.com");
        author.setBiography("Author Biography");
        author.addBook(new Book(){});
        authorRepository.save(author);
        }

        //create Book
        if (bookRepository.findByTitle("Book1").isEmpty()) {

            var book = new Book();
            book.setTitle("Book1");
            book.setDescription("This is a book");
            book.setEdition("Edition1");
            book.setGenre("Genre1");
            book.setLanguage("Language1");
            book.setPrice(100.0);
            book.setPages(10);
            book.setPublicationDate(new Date());
            book.setTitle("Title1");
            book.setPublisher("Publisher1");
            book.setOwner(null);
            book.setQuantity("10");
            bookRepository.save(book);
        }



        var resultAction = this.mockMvc
                .perform(post(this.baseUrl + "/users/login").with(httpBasic("ben", "123456")));
        var result = resultAction.andDo(print()).andReturn();
        var resultContentString = result.getResponse().getContentAsString();
        var json = new JSONObject(resultContentString);
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
                "test@email.com",
                "biography1",
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
                .andExpect(jsonPath("$.data.email").value("test@email.com"))
                .andExpect(jsonPath("$.data.biography").value("biography1"))
                .andExpect(jsonPath("$.data.numberOfBooks").isNotEmpty());
    }

    @Test
    @DisplayName("Check Get all Authors (GET)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllAuthors() throws Exception {
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

        //create an author
        var author = new AuthorDto(
                null,
                "name1",
                "name11",
                "name1@email.com",
                "biogrpahy1",
                null
        );

        var json = this.objectMapper.writeValueAsString(author);
        var postResult = this.mockMvc.perform(post(baseUrl + "/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS)).andReturn();

        var responseContent = postResult.getResponse().getContentAsString();
        var responseJson = new JSONObject(responseContent);
        var authorId = responseJson.getJSONObject("data").getString("authorId");

        //get author by Id
        this.mockMvc.perform(get(this.baseUrl + "/authors/{authorId}", authorId)
        .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.authorId").value(authorId));
    }

    @Test
    @DisplayName("Check findAuthorByIdFailure (GET)")
    void testFindAuthorByIdFailure() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/authors/10")
        .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find author with Id 10"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check updateBook with valid input (put)")
    void testUpdateAuthorSuccess() throws Exception {

        //create an author
        var author = new AuthorDto(
                null,
                "paul",
                "bin",
                "testemail1@email.com",
                "biogrpahy1",
                null
        );

        var json = this.objectMapper.writeValueAsString(author);
        var postResult = this.mockMvc.perform(post(baseUrl + "/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS)).andReturn();

        var responseContent = postResult.getResponse().getContentAsString();
        var responseJson = new JSONObject(responseContent);
        var authorId = responseJson.getJSONObject("data").getString("authorId");


        var authorDto = new AuthorDto(
                null,
                "paul",
                "bin",
                "testemail1@email.com",
                "biography1 updated",
                null
        );

        var jsonUpdate = this.objectMapper.writeValueAsString(authorDto);
        this.mockMvc.perform(put(baseUrl + "/authors/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.authorId").isNotEmpty())
                .andExpect(jsonPath("$.data.firstName").value("paul"))
                .andExpect(jsonPath("$.data.biography").value("biography1 updated"));
    }

    @Test
    @DisplayName("Check deleteAuthor with valid authorId (Delete)")
    void testDeleteAuthorSuccess() throws Exception {

        //create an author to delete
        //create an author
        var author = new AuthorDto(
                null,
                "tayo",
                "fred",
                "fred@email.com",
                "some bio",
                null
        );

        var createdAuthorJson = this.objectMapper.writeValueAsString(author);
        var result = this.mockMvc.perform(post(baseUrl + "/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createdAuthorJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS)).andReturn();

        // Extract the authorId from the POST response
        var resultContent = result.getResponse().getContentAsString();

        //Parse the JSON response using JSONObject (or any preferred JSON library)
        JSONObject jsonResponse = new JSONObject(resultContent);

        // Extract the authorId from the "data" object
        Long authorId = jsonResponse.getJSONObject("data").getLong("authorId");

        this.mockMvc.perform(delete(this.baseUrl + "/authors/{authorId}", authorId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));
        this.mockMvc.perform(get(this.baseUrl + "/authors/{authorId}", authorId)
                        .accept(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find author with Id " + authorId))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    @DisplayName("Check deleteAuthor with invalid authorId (Delete)")
    void testDeleteAuthorFailureWithNonExistingId() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/authors/7")
                        .accept(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find author with Id 7"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check assign book to author with valid Ids (Put)")
    void testAssignAuthorSuccess() throws Exception{

        //create book
        //UUID bookId = UUID.randomUUID();
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


        //create an author
        var author = new AuthorDto(
                null,
                "paul",
                "bin",
                "some@email.com",
                "biogrpahy1",
                null
        );

        var createdAuthorJson = this.objectMapper.writeValueAsString(author);
        var result = this.mockMvc.perform(post(baseUrl + "/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createdAuthorJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS)).andReturn();

        // Extract the authorId from the POST response
        var resultContent = result.getResponse().getContentAsString();

        //Parse the JSON response using JSONObject (or any preferred JSON library)
        JSONObject jsonResponse = new JSONObject(resultContent);

        // Extract the authorId from the "data" object
        Long authorId = jsonResponse.getJSONObject("data").getLong("authorId");


        this.mockMvc.perform(put(this.baseUrl + "/authors/{authorId}/books/{bookId}", authorId, bookId )
                        .accept(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Book Assignment Success"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

}
