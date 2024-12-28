package com.tjtechy.tjtechyinventorymanagementsept2024.book.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.repository.AuthorRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.repository.BookRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.ChatClient;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
public class BookControllerTestContainerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibraryUserRepository libraryUserRepository;

    @Autowired
    private AuthorRepository authorRepository;



    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;



    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    private final static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.0")
            .withDatabaseName("inventory-management-sept2024")
            .withUsername("postgres")
            .withPassword("postgres");
    @DynamicPropertySource
    static void postgreSQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }
    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }
    @AfterAll
    static void tearDown() {
        postgreSQLContainer.stop();
    }
    @BeforeEach
    void setUp() throws Exception {

        //this script executes the sql script
        try (var connection = DriverManager.getConnection(postgreSQLContainer.getJdbcUrl(),
                postgreSQLContainer.getUsername(),
                postgreSQLContainer.getPassword())) {
            var schemaScript = Files.readString(Path.of("src/test/resources/schema.sql"));
            try (var statement = connection.createStatement()) {
                statement.execute(schemaScript);
            }
        }

        //create the Admin user: ben with password: 123456
        if(libraryUserRepository.findByUserName("ken").isEmpty()) {
            var adminUser = new LibraryUser();
            adminUser.setUserName("ken");
            adminUser.setPassword(passwordEncoder.encode("123456"));
            adminUser.setRoles("Admin user");
            adminUser.setEnabled(true);
            libraryUserRepository.save(adminUser);
        }

        //create Author
        if(authorRepository.findByFirstNameAndLastName("Name1", "Name2").isEmpty()) {
            var author = new Author();
            author.setFirstName("Name1");
            author.setLastName("Name2");
            author.setEmail("name1@gmail.com");
            author.setBiography("Name Biography");
            author.addBook(new Book(){});
            authorRepository.save(author);
        }

        //create Book
        if (bookRepository.findByTitle("Book").isEmpty()) {

            var book = new Book();
            book.setTitle("Book");
            book.setDescription("This is a book");
            book.setEdition("Edition");
            book.setGenre("Genre");
            book.setLanguage("Language");
            book.setPrice(100.0);
            book.setPages(10);
            book.setPublicationDate(new Date());
            book.setTitle("Title");
            book.setPublisher("Publisher");
            book.setOwner(null);
            book.setQuantity("10");
            bookRepository.save(book);
        }

        var resultAction = this.mockMvc
                .perform(post(this.baseUrl + "/users/login").with(httpBasic("ken", "123456")));
        var result = resultAction.andDo(print()).andReturn();
        var resultContentString = result.getResponse().getContentAsString();
        var json = new JSONObject(resultContentString);
        this.token = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    @DisplayName("Check Add Book (POST) and Find book by Id")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddBookAndFindById() throws Exception {
        var date = new Date(1726680002000L);
        var book = new BookDto(
               null,
                "title1",
                date,
                "john",
                "fiction",
                "first",
                "yoruba",
                500,
                "some description",
                100.0,
                "20",
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
                .andExpect(jsonPath("$.data.title").value(book.title()))
                .andExpect(jsonPath("$.data.description").value(book.description()))
                .andExpect(jsonPath("$.data.edition").value(book.edition()))
                .andExpect(jsonPath("$.data.language").value(book.language()))
                .andExpect(jsonPath("$.data.price").value(book.price()))
                .andExpect(jsonPath("$.data.pages").value(book.pages()))
                .andExpect(jsonPath("$.data.publicationDate").isNotEmpty())
                .andExpect(jsonPath("$.data.Quantity").value(book.Quantity()))
                .andReturn();

        //use postResult to do some other things, for example if you need to extract any data to print in the console
        // Extract the bookId (UUID) from the POST response
        var responseContent = postResult.getResponse().getContentAsString();
        var response = new JSONObject(responseContent);
        var bookId = response.getJSONObject("data").getString("ISBN");

        //find by id
        this.mockMvc.perform(get(this.baseUrl + "/books/" + bookId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.title").value(book.title()))
                .andExpect(jsonPath("$.data.description").value(book.description()))
                .andExpect(jsonPath("$.data.edition").value(book.edition()))
                .andExpect(jsonPath("$.data.language").value(book.language()))
                .andExpect(jsonPath("$.data.price").value(book.price()))
                .andExpect(jsonPath("$.data.pages").value(book.pages()))
                .andExpect(jsonPath("$.data.publicationDate").isNotEmpty())
                .andExpect(jsonPath("$.data.Quantity").value(book.Quantity()))
                .andExpect(jsonPath("$.data.ISBN").value(bookId));

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllBooksSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/books").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"));

    }

    @Test
    @DisplayName("Check updateBook with valid input (put)")
    void testUpdateBookWithValidInput() throws Exception {
        //create a book
        var date = new Date(1726680002000L);
        var book = new BookDto(

                null,
                "title1",
                date,
                "john",
                "fiction",
                "first",
                "yoruba",
                500,
                "some description",
                100.0,
                "20", null
        );

        var json = this.objectMapper.writeValueAsString(book);
        var postResult = this.mockMvc.perform(post(this.baseUrl + "/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS)).andReturn();

        // Extract the bookId (UUID) from the POST response
        var responseContent = postResult.getResponse().getContentAsString();
        var response = new JSONObject(responseContent);
        var bookId = response.getJSONObject("data").getString("ISBN");

        //update
        var bookUpdate = new BookDto(

                null,
                "title1",
                date,
                "john",
                "fiction updated",
                "first",
                "yoruba",
                500,
                "some description",
                100.0,
                "20", null
        );
        var jsonBookUpdated = this.objectMapper.writeValueAsString(bookUpdate);
        this.mockMvc.perform(put(this.baseUrl + "/books/" + bookId)
        .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBookUpdated)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.ISBN").value(bookId))
                .andExpect(jsonPath("$.data.Genre").value(bookUpdate.Genre()));
    }


    @Test
    @DisplayName("Check Delete book with valid ISBN (Delete)")
    void testDeleteBookWithValidInput() throws Exception {

        //create a book to be deleted
        var date = new Date(1726680002000L);
        var book = new BookDto(

                null,
                "title1",
                date,
                "john",
                "fiction",
                "first",
                "yoruba",
                500,
                "some description",
                100.0,
                "20", null
        );

        var json = this.objectMapper.writeValueAsString(book);
        var postResult = this.mockMvc.perform(post(this.baseUrl + "/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS)).andReturn();

        // Extract the bookId (UUID) from the POST response
        var responseContent = postResult.getResponse().getContentAsString();
        var response = new JSONObject(responseContent);
        var bookId = response.getJSONObject("data").getString("ISBN");

        //Delete
        this.mockMvc.perform(delete(this.baseUrl + "/books/" + bookId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));
    }

    @Test
    @DisplayName("Check find book by criteria (POST)")
    void testFindBookByDescription() throws Exception {
        //Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("description", "book1 description");

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "title,desc");


        //serialize the searchCriteria
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        //When and Then
        this.mockMvc.perform(post(this.baseUrl + "/books/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                        .params(requestParams)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search Success"));
    }

    @Test
    @DisplayName("Check find book by criteria (POST)")
    void testFindBookByTitleAndDescription() throws Exception {
        //Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("title", "book1");
        searchCriteria.put("description", "book1 description");

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "title,desc");

        //serialize the searchCriteria
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        //When and Then
        this.mockMvc.perform(post(this.baseUrl + "/books/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .params(requestParams)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search Success"));

    }



}
