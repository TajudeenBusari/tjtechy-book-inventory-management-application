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
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/***
 * Integration tests for Library User API endpoints using test containers
 * In this test class avoid creating user with same name for different test methods.
 * Remember to start redis else all integration tests will fail
 * A logic to run integration test without starting redis will be
 * implemented in the future
 */
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

//    @Container
//
//    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));



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
    @DisplayName("Check findLibraryUser with valid Id (GET): User with ROLE_user accessing own details")
    void testFindLibraryUserWithUserAccessingOwnInfo() throws Exception {
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

        //login as user and extract token
        var kukuResultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("kuku", "654321")));
        var kukuResult = kukuResultAction.andDo(print()).andReturn();
        var kukuContentString = kukuResult.getResponse().getContentAsString();
        var kukuJsonString = new JSONObject(kukuContentString);
        var kukuToken = "Bearer " + kukuJsonString.getJSONObject("data").getString("token");

        //Normal user finding the user by id
        this.mockMvc.perform(get(this.baseUrl + "/users/" + userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, kukuToken))
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
     * Logic has been updated so that Admin can update any user info and
     * user can update own info
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

        //Admin updating user info
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
    @DisplayName("Check updateLibraryUser with valid input (PUT): with Role_user Updating own info")
    void testUpdateLibraryUserWithUserUpdatingOwnInfo() throws Exception {
        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("sam");
        libraryUser.setPassword("098765");
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

        //login as user and extract token
        var samuelResultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("sam", "098765")));
        var samuelResult = samuelResultAction.andDo(print()).andReturn();
        var samuelContentString = samuelResult.getResponse().getContentAsString();
        var samuelJsonString = new JSONObject(samuelContentString);
        var samuelToken = "Bearer " + samuelJsonString.getJSONObject("data").getString("token");

        //Normal user updating own info
        var normalUser = new LibraryUser();
        normalUser.setUserName("samUpdate");
        normalUser.setEnabled(true);
        normalUser.setRoles("user");
        var samuelJson = objectMapper.writeValueAsString(normalUser);
        this.mockMvc.perform(put(this.baseUrl + "/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(samuelJson)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, samuelToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"));
    }

    @Test
    @DisplayName("Check updateLibraryUser with valid input (PUT): with Role_user Updating Another User info")
    void testUpdateLibraryUserWithUserUpdatingAnotherUserInfo() throws Exception {
        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("chris");
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


        //Normal user updating another user info
        var update = new LibraryUser();
        update.setUserName("christopher"); //updated username
        update.setEnabled(true);
        update.setRoles("user");
        var cJson = objectMapper.writeValueAsString(update);
        this.mockMvc.perform(put(this.baseUrl + "/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cJson)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.nToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"))
                .andExpect(jsonPath("$.data").value("Access Denied"));
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

    @Test
    @DisplayName("Check Password change with valid input (PATCH)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testChangePasswordSuccess() throws Exception{
        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("jane");
        libraryUser.setPassword("Abc123456");
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

        //jane login and extract token
        var janeResultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("jane", "Abc123456")));
        var janeResult = janeResultAction.andDo(print()).andReturn();
        var janeContentString = janeResult.getResponse().getContentAsString();
        var janeJsonString = new JSONObject(janeContentString);
        var janeToken = "Bearer " + janeJsonString.getJSONObject("data").getString("token");

        //change password
        var changePassword = new JSONObject();
        changePassword.put("oldPassword", "Abc123456");
        changePassword.put("newPassword", "Abc1234567");
        changePassword.put("confirmNewPassword", "Abc1234567");
        var changePasswordJson = changePassword.toString();
        /**
         * It is also possible to use:
         * Map<String, String> changePassword = new HashMap<>();
         * changePassword.put("oldPassword", "Abc123456");
         * changePassword.put("newPassword", "Abc1234567");
         * changePassword.put("confirmNewPassword", "Abc1234567");
         * var changePasswordJson = objectMapper.writeValueAsString(changePassword);
         * */

        this.mockMvc.perform(patch(this.baseUrl + "/users/" + userId + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, janeToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Password Change Success"));

    }

    @Test
    @DisplayName("Check Password change with wrong old password (PATCH)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testChangePasswordWithWrongOldPassword() throws Exception{
        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("mary");
        libraryUser.setPassword("Def123456");
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

        //mary login and extract token
        var janeResultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("mary", "Def123456")));
        var janeResult = janeResultAction.andDo(print()).andReturn();
        var janeContentString = janeResult.getResponse().getContentAsString();
        var janeJsonString = new JSONObject(janeContentString);
        var maryToken = "Bearer " + janeJsonString.getJSONObject("data").getString("token");

        //change password
        var changePassword = new JSONObject();
        changePassword.put("oldPassword", "Abc1234567"); //wrong old password, it is Def123456
        changePassword.put("newPassword", "Def1234567");
        changePassword.put("confirmNewPassword", "Def1234567");
        var changePasswordJson = changePassword.toString();

        this.mockMvc.perform(patch(this.baseUrl + "/users/" + userId + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, maryToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("username or password is incorrect."))
                .andExpect(jsonPath("$.data").value("Old password is incorrect."));
    }

    @Test
    @DisplayName("Check Password change with new password and confirm new password not matching (PATCH)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testPasswordChangeWithNewPasswordAndConfirmNewPasswordNotMatching() throws Exception{
        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("greg");
        libraryUser.setPassword("Abc123456");
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

        //greg login and extract token
        var gregResultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("greg", "Abc123456")));
        var gregResult = gregResultAction.andDo(print()).andReturn();
        var gregContentString = gregResult.getResponse().getContentAsString();
        var gregJsonString = new JSONObject(gregContentString);
        var gregToken = "Bearer " + gregJsonString.getJSONObject("data").getString("token");

        //change password
        var changePassword = new JSONObject();
        changePassword.put("oldPassword", "Abc123456");
        changePassword.put("newPassword", "Abc1234567");
        changePassword.put("confirmNewPassword", "Abc12345678"); //confirm new password not matching
        var changePasswordJson = changePassword.toString();

        this.mockMvc.perform(patch(this.baseUrl + "/users/" + userId + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, gregToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.BAD_REQUEST))
                .andExpect(jsonPath("$.message").value("Password change failed"))
                .andExpect(jsonPath("$.data").value("New password and confirm new password do not match."));
    }

    @Test
    @DisplayName("Check Password change with new password not conforming to password policy (PATCH)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testPasswordChangeWithNewPasswordNotConformingToPasswordPolicy() throws Exception{
        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("james");
        libraryUser.setPassword("Abc123456");
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

        //james login and extract token
        var jamesResultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("james", "Abc123456")));
        var jamesResult = jamesResultAction.andDo(print()).andReturn();
        var jamesContentString = jamesResult.getResponse().getContentAsString();
        var jamesJsonString = new JSONObject(jamesContentString);
        var jamesToken = "Bearer " + jamesJsonString.getJSONObject("data").getString("token");

        //change password
        var changePassword = new JSONObject();
        changePassword.put("oldPassword", "Abc123456");
        changePassword.put("newPassword", "abc1234567"); //new password does not conform to password policy
        changePassword.put("confirmNewPassword", "abc1234567");
        var changePasswordJson = changePassword.toString();

        this.mockMvc.perform(patch(this.baseUrl + "/users/" + userId + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, jamesToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.BAD_REQUEST))
                .andExpect(jsonPath("$.message").value("Password change failed"))
                .andExpect(jsonPath("$.data").value("New password must contain at least 8 characters, at least 1 digit, at least 1 lowercase letter, at least 1 uppercase letter."));
    }

    @Test
    @DisplayName("Check Password change with new password same as old password (PATCH)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testPasswordChangeWithNewPasswordSameAsOldPassword() throws Exception{
        //create a user
        var libraryUser = new LibraryUser();
        libraryUser.setUserName("atika");
        libraryUser.setPassword("Abc123456");
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

        //atika login and extract token
        var atikaResultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("atika", "Abc123456")));
        var atikaResult = atikaResultAction.andDo(print()).andReturn();
        var atikaContentString = atikaResult.getResponse().getContentAsString();
        var atikaJsonString = new JSONObject(atikaContentString);
        var atikaToken = "Bearer " + atikaJsonString.getJSONObject("data").getString("token");

        //change password
        var changePassword = new JSONObject();
        changePassword.put("oldPassword", "Abc123456");
        changePassword.put("newPassword", "Abc123456"); //new password is same as old password
        changePassword.put("confirmNewPassword", "Abc123456");
        var changePasswordJson = changePassword.toString();

        this.mockMvc.perform(patch(this.baseUrl + "/users/" + userId + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, atikaToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.BAD_REQUEST))
                .andExpect(jsonPath("$.message").value("Password change failed"))
                .andExpect(jsonPath("$.data").value("New password must be different from the old password."));
    }

    @Test
    @DisplayName("Check Password change with user not found (PATCH)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testPasswordChangeWithUserNotFound() throws Exception{
        //create a user
        var NonExistingUserId = 1000;

        //Change password
        var changePassword = new JSONObject();
        changePassword.put("oldPassword", "Abc123456");
        changePassword.put("newPassword", "Abc1234567");
        changePassword.put("confirmNewPassword", "Abc1234567");
        var changePasswordJson = changePassword.toString();

        //since user does not exist, we can use Admin to change password
        this.mockMvc.perform(patch(this.baseUrl + "/users/" + NonExistingUserId + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.aToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find library user with Id " + NonExistingUserId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

}
