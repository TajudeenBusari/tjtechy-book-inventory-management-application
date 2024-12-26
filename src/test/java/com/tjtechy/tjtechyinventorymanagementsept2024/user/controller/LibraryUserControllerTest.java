package com.tjtechy.tjtechyinventorymanagementsept2024.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.LibraryUserNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.dto.LibraryUserDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.service.LibraryUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(LibraryUserController.class)
//@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)//turns off spring security
/**
 * this will override the active profile in application.yml file.
 * Irrespective of the active profile, the test will only run using the
 * h2-database
 *
 */
@ActiveProfiles(value = "h2-database")
class LibraryUserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LibraryUserService libraryUserService;

    @Autowired
    ObjectMapper objectMapper;

    List<LibraryUser> libraryUsers;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {
        libraryUsers = new ArrayList<>();

        var libraryUser1 = new LibraryUser();
        libraryUser1.setUserId(1);
        libraryUser1.setUserName("test1");
        libraryUser1.setPassword("password1");
        libraryUser1.setEnabled(true);
        libraryUser1.setRoles("admin");
        libraryUsers.add(libraryUser1);

        var libraryUser2 = new LibraryUser();
        libraryUser2.setUserId(2);
        libraryUser2.setUserName("test2");
        libraryUser2.setPassword("password2");
        libraryUser2.setEnabled(false);
        libraryUser2.setRoles("user");
        libraryUsers.add(libraryUser2);

        var libraryUser3 = new LibraryUser();
        libraryUser3.setUserId(3);
        libraryUser3.setUserName("test3");
        libraryUser3.setPassword("password3");
        libraryUser3.setEnabled(true);
        libraryUser3.setRoles("admin");
        libraryUsers.add(libraryUser3);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testAddLibraryUserSuccess() throws Exception {
        //Given
        //prepare fake data to add
        var libraryUser = new LibraryUser();
        libraryUser.setUserId(1);
        libraryUser.setUserName("username1");
        libraryUser.setPassword("password1");
        libraryUser.setEnabled(true);
        libraryUser.setRoles("user");

        var json = objectMapper.writeValueAsString(libraryUser);

        //data the service layer will return
        var savedLibraryuser = new LibraryUser();
        savedLibraryuser.setUserId(1);
        savedLibraryuser.setUserName("username1");
        savedLibraryuser.setPassword("password1");

        savedLibraryuser.setEnabled(true);
        savedLibraryuser.setRoles("user");

        given(this.libraryUserService.save(Mockito.any(LibraryUser.class))).willReturn(savedLibraryuser);

        //When and //Then
        this.mockMvc.perform(post(baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.userId").isNotEmpty())
                .andExpect(jsonPath("$.data.userName").value(savedLibraryuser.getUserName()))
                .andExpect(jsonPath("$.data.enabled").value(savedLibraryuser.isEnabled()))
                .andExpect(jsonPath("$.data.roles").value(savedLibraryuser.getRoles()));

    }

    @Test
    void testFindLibraryUserByIdSuccess() throws Exception {
        //Given
        given(this.libraryUserService.findById(1)).willReturn(libraryUsers.get(0));

        //When and Then
        this.mockMvc.perform(get(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.userName").value("test1"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("admin"));
    }

    @Test
    void testFindLibraryUserByIdNotFound() throws Exception {
        //Given
        given(this.libraryUserService.findById(4)).willThrow(new LibraryUserNotFoundException(4));//this id does not exist in our list array

        //When and Then
        this.mockMvc.perform(get(baseUrl + "/users/4").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find library user with Id 4"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllLibraryUsersSuccess() throws Exception {
        //Given
        given(this.libraryUserService.findAll()).willReturn(libraryUsers);

        //When and Then
        this.mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].userName").value("test1"))
                .andExpect(jsonPath("$.data[0].enabled").value(true))
                .andExpect(jsonPath("$.data[0].roles").value("admin"))
                .andExpect(jsonPath("$.data[1].userId").value(2))
                .andExpect(jsonPath("$.data[1].userName").value("test2"))
                .andExpect(jsonPath("$.data[1].enabled").value(false))
                .andExpect(jsonPath("$.data[1].roles").value("user"))
                .andExpect(jsonPath("$.data[2].userId").value(3))
                .andExpect(jsonPath("$.data[2].userName").value("test3"))
                .andExpect(jsonPath("$.data[2].enabled").value(true))
                .andExpect(jsonPath("$.data[2].roles").value("admin"));

    }

    @Test
    void testUpdateLibraryUserSuccess() throws Exception {
        //Given
        var libraryUserDto = new LibraryUserDto(
                1, "tom123", true, "user"
        );

        //data that the service will return
        var updatedLibraryUser = new LibraryUser();
        updatedLibraryUser.setUserId(1);
        updatedLibraryUser.setUserName("tom123"); //lets assume it tom
        updatedLibraryUser.setEnabled(true);
        updatedLibraryUser.setRoles("user");

        var json = objectMapper.writeValueAsString(libraryUserDto);
        given(this.libraryUserService.update(Mockito.any(LibraryUser.class), eq(1))).willReturn(updatedLibraryUser);

        //When and Then
        this.mockMvc.perform(put(baseUrl + "/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.userName").value(updatedLibraryUser.getUserName()))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value(updatedLibraryUser.getRoles()));
    }

    @Test
    void testUpdateLibraryUserNotFound() throws Exception {
        //Given
        given(this.libraryUserService.update(Mockito.any(LibraryUser.class), eq(1))).willThrow(new LibraryUserNotFoundException(1));

        var libraryUserDto = new LibraryUserDto(
                1, "tom123", false, "user"
        );

        var json = objectMapper.writeValueAsString(libraryUserDto);

        //When and Then
        this.mockMvc.perform(put(baseUrl + "/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find library user with Id 1"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void testDeleteLibraryUserSuccess() throws Exception {
        //Given
        doNothing().when(this.libraryUserService).delete(1);

        //When and Then
        this.mockMvc.perform(delete(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void testDeleteLibraryUserNotFound() throws Exception {
        //Given
        doThrow(new LibraryUserNotFoundException(1)).when(this.libraryUserService).delete(1);

        //When and Then
        this.mockMvc.perform(delete(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find library user with Id 1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}