package com.tjtechy.tjtechyinventorymanagementsept2024.user.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Tag("Integration")
@ActiveProfiles(value = "postgre-database-test")
@DisplayName("Integration tests for Library User API endpoints using test containers")
public class LibraryUserTestContainerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibraryUserRepository libraryUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    @Autowired
    private ObjectMapper objectMapper;

    private String aToken;
    private String nToken;

    private final static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.0")
            .withDatabaseName("inventory-management-sept2024")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

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
         * without going through the controller
         */

        //create an Admin user
        if(libraryUserRepository.findByUserName("taju").isEmpty()){
            var adminUser = new LibraryUser();
            adminUser.setUserName("taju");
            adminUser.setPassword(passwordEncoder.encode("123456"));
            adminUser.setRoles("Admin user");
            adminUser.setEnabled(true);
            libraryUserRepository.save(adminUser);
        }

        //create a normal user
        if(libraryUserRepository.findByUserName("doe").isEmpty()){
            var normalUser = new LibraryUser();
            normalUser.setUserName("doe");
            normalUser.setPassword(passwordEncoder.encode("654321"));
            normalUser.setRoles("user");
            normalUser.setEnabled(true);
            libraryUserRepository.save(normalUser);
        }

        //admin token
        var resultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("taju", "123456")));
        var result = resultAction.andDo(print()).andReturn();
        var contentString = result.getResponse().getContentAsString();
        var jsonString = new JSONObject(contentString);
        this.aToken = "Bearer " + jsonString.getJSONObject("data").getString("token");

        //user token
        var nResultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("doe", "654321")));
        var nResult = nResultAction.andDo(print()).andReturn();
        var nContentString = nResult.getResponse().getContentAsString();
        var nJsonString = new JSONObject(nContentString);
         this.nToken = "Bearer " + nJsonString.getJSONObject("data").getString("token");

    }
    @Test
    @DisplayName("Check add LibraryUser with valid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddUser() throws Exception {
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("user1");
        libraryUser.setPassword("123456");
        libraryUser.setRoles("user");
        libraryUser.setEnabled(true);

        var json = objectMapper.writeValueAsString(libraryUser);
        this.mockMvc.perform(post(this.baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"));
    }

    @Test
    @DisplayName("Check findAllLibrarysers (GET)")
    void testFindAllLibraryUsersSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"));

    }

    @Test
    @DisplayName("Check findLibraryUser with valid Id (GET): User with ROLE_Admin accessing any user details")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindLibraryUserByIdSuccess() throws Exception {
        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("moro");
        libraryUser.setPassword("654321");
        libraryUser.setRoles("user");
        libraryUser.setEnabled(true);

        var json = objectMapper.writeValueAsString(libraryUser);
        var postResult = this.mockMvc.perform(post(this.baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success")).andReturn();
        var responseContent = postResult.getResponse().getContentAsString();
        var jsonString = new JSONObject(responseContent);
        var userId = jsonString.getJSONObject("data").getString("userId");

        //Admin finding the user by id
        this.mockMvc.perform(get(this.baseUrl + "/users/" + userId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"));
    }

    @Test
    @DisplayName("Check findLibraryUser with valid Id (GET): User with ROLE_user Accessing Another LibraryUser's Info")
    void testFindLibraryUserWithUserAccessingAnyLibraryUserAccessDenied() throws Exception {

        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("kuku");
        libraryUser.setPassword("654321");
        libraryUser.setRoles("user");
        libraryUser.setEnabled(true);

        var json = objectMapper.writeValueAsString(libraryUser);
        var postResult = this.mockMvc.perform(post(this.baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success")).andReturn();
        var responseContent = postResult.getResponse().getContentAsString();
        var jsonString = new JSONObject(responseContent);
        var userId = jsonString.getJSONObject("data").getString("userId");

        //Normal user finding the user by id
        this.mockMvc.perform(get(this.baseUrl + "/users/" + userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.nToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"))
                .andExpect(jsonPath("$.data").value("Access Denied"));

    }

    /***
     * for now it is only Admin that can update or delete user
     *
     */
    @Test
    @DisplayName("Check updateLibraryUser with valid input (PUT): with Role_Admin Updating own or any user info")
    void testUpdateLibraryUserWithAdminUpdatingOwnOrAnyUserInfo() throws Exception {
        //create a user-->Only Admin can add user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("inayah");
        libraryUser.setPassword("654321");
        libraryUser.setRoles("user");
        libraryUser.setEnabled(true);

        var json = objectMapper.writeValueAsString(libraryUser);
        var postResult = this.mockMvc.perform(post(this.baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success")).andReturn();
        var responseContent = postResult.getResponse().getContentAsString();
        var jsonString = new JSONObject(responseContent);
        var userId = jsonString.getJSONObject("data").getString("userId");

        //user updating own info
        var normalUser = new LibraryUser();
        normalUser.setUserName("inayahUpdate");
        normalUser.setEnabled(true);
        normalUser.setRoles("user");
        var inayahJson = objectMapper.writeValueAsString(normalUser);
        this.mockMvc.perform(put(this.baseUrl + "/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(inayahJson)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"));

    }

    @Test
    @DisplayName("Check deleteLibraryUser with valid input (DELETE)")
    void testDeleteLibraryUserWithValidInput() throws Exception {
        //create a user to be deleted
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("femi");
        libraryUser.setPassword("654321");
        libraryUser.setRoles("Admin user");
        libraryUser.setEnabled(true);

        var json = objectMapper.writeValueAsString(libraryUser);
        var postResult = this.mockMvc.perform(post(this.baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success")).andReturn();
        var responseContent = postResult.getResponse().getContentAsString();
        var jsonString = new JSONObject(responseContent);
        var userId = jsonString.getJSONObject("data").getString("userId");

        //delete user
        this.mockMvc.perform(delete(this.baseUrl + "/users/" + userId)
        .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteLibraryUser with insufficient permission (DELETE)")
    void testDeleteLibraryUserWithInsufficientPermission() throws Exception {
        //create a user to be deleted
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("kemi");
        libraryUser.setPassword("654321");
        libraryUser.setRoles("user");
        libraryUser.setEnabled(true);

        var json = objectMapper.writeValueAsString(libraryUser);
        var postResult = this.mockMvc.perform(post(this.baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success")).andReturn();
        var responseContent = postResult.getResponse().getContentAsString();
        var jsonString = new JSONObject(responseContent);
        var userId = jsonString.getJSONObject("data").getString("userId");

        //delete user with normal user role
        this.mockMvc.perform(delete(this.baseUrl + "/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.nToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"));
    }

}
