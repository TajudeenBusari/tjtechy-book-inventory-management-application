package com.tjtechy.tjtechyinventorymanagementsept2024.user.service;

import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.LibraryUserNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.repository.LibraryUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryUserServiceTest {

    @Mock
    LibraryUserRepository libraryUserRepository;

    @InjectMocks
    LibraryUserService libraryUserService;

    List<LibraryUser> libraryUsers;

    @BeforeEach
    void setUp() {
        libraryUsers = new ArrayList<>(); //initialize array with null

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
    void testSaveLibraryUserSuccess() {
        //Given
        var newLibraryUser = new LibraryUser();
        newLibraryUser.setUserId(1);
        newLibraryUser.setUserName("user1");
        newLibraryUser.setPassword("password1");

        newLibraryUser.setEnabled(true);
        newLibraryUser.setRoles("role1");

        given(libraryUserRepository.save(newLibraryUser)).willReturn(newLibraryUser);

        //When
        var savedLibraryUser = libraryUserService.save(newLibraryUser);

        //Then
        assertThat(savedLibraryUser.getUserId()).isEqualTo(newLibraryUser.getUserId());
        assertThat(savedLibraryUser.getUserName()).isEqualTo(newLibraryUser.getUserName());
        assertThat(savedLibraryUser.getPassword()).isEqualTo(newLibraryUser.getPassword());

        assertThat(savedLibraryUser.isEnabled()).isEqualTo(newLibraryUser.isEnabled());
        assertThat(savedLibraryUser.getRoles()).isEqualTo(newLibraryUser.getRoles());

        verify(libraryUserRepository, times(1)).save(newLibraryUser);
    }

    @Test
    void testFindLibraryUserByIdSuccess() {

        // Given. Arrange inputs and targets. Define the behavior of Mock object userRepository.
        var libraryUser = new LibraryUser();
        libraryUser.setUserId(1);
        libraryUser.setUserName("user1");
        libraryUser.setPassword("password1");
        libraryUser.setEnabled(true);
        libraryUser.setRoles("Admin user");

        given(this.libraryUserRepository.findById(libraryUser.getUserId())).willReturn(Optional.of(libraryUser));

        //When
        LibraryUser returnedUser = this.libraryUserService.findById(libraryUser.getUserId());

        //Then
        assertThat(returnedUser.getUserId()).isEqualTo(libraryUser.getUserId());
        assertThat(returnedUser.getUserName()).isEqualTo(libraryUser.getUserName());
        assertThat(returnedUser.getPassword()).isEqualTo(libraryUser.getPassword());
        assertThat(returnedUser.isEnabled()).isEqualTo(libraryUser.isEnabled());
        assertThat(returnedUser.getRoles()).isEqualTo(libraryUser.getRoles());
        verify(libraryUserRepository, times(1)).findById(libraryUser.getUserId());
    }

    @Test
    void testFindLibraryUserByIdNotFound() {
        //Given
        given(this.libraryUserRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

        //When
        Throwable thrown = catchThrowable(() ->{
            var libraryUser = this.libraryUserService.findById(1);
        } );

        //Then
        assertThat(thrown)
                .isInstanceOf(LibraryUserNotFoundException.class)
                .hasMessage("Could not find library user with Id 1");

        verify(libraryUserRepository, times(1)).findById(Mockito.any(Integer.class));
    }

    @Test
    void testFindAllLibraryUsersSuccess() {
        //Given
        given(this.libraryUserRepository.findAll()).willReturn(libraryUsers);
        //When
        List<LibraryUser> returnedUsers = this.libraryUserService.findAll();

        //Then
        assertThat(returnedUsers.size()).isEqualTo(libraryUsers.size());

        verify(libraryUserRepository, times(1)).findAll();
    }

    @Test
    void testUpdateLibraryUserSuccess() {
        //Given
        var oldLibraryUser = new LibraryUser();
        oldLibraryUser.setUserId(1);
        oldLibraryUser.setUserName("user1");
        oldLibraryUser.setPassword("password1");
        oldLibraryUser.setEnabled(true);
        oldLibraryUser.setRoles("user");

        var updateLibraryUser = new LibraryUser();
        updateLibraryUser.setUserId(1);
        updateLibraryUser.setUserName("user1-update");
        updateLibraryUser.setPassword("password1");
        updateLibraryUser.setEnabled(true);
        updateLibraryUser.setRoles("admin user");

        //first find if user exists and save
        given(libraryUserRepository.findById(oldLibraryUser.getUserId())).willReturn(Optional.of(oldLibraryUser));
        given(libraryUserRepository.save(oldLibraryUser)).willReturn(oldLibraryUser);

        //When
        LibraryUser updatedLibraryUser = libraryUserService.update(updateLibraryUser, oldLibraryUser.getUserId());

        //then
        assertThat(updatedLibraryUser.getUserId()).isEqualTo(updateLibraryUser.getUserId());
        assertThat(updatedLibraryUser.getUserName()).isEqualTo(updateLibraryUser.getUserName());
        assertThat(updatedLibraryUser.getPassword()).isEqualTo(updateLibraryUser.getPassword());
        assertThat(updatedLibraryUser.isEnabled()).isEqualTo(updateLibraryUser.isEnabled());
        assertThat(updatedLibraryUser.getRoles()).isEqualTo(updateLibraryUser.getRoles());
        verify(libraryUserRepository, times(1)).findById(oldLibraryUser.getUserId());
    }

    @Test
    void testUpdateLibraryUserNotFound() {
        //Given
        //The old does not even exist

        var updateLibraryUser = new LibraryUser();
        updateLibraryUser.setUserName("user1-update");
        updateLibraryUser.setPassword("password1");
        updateLibraryUser.setEnabled(true);
        updateLibraryUser.setRoles("admin user");

        given(libraryUserRepository.findById(updateLibraryUser.getUserId())).willReturn(Optional.empty());

        //When
        assertThrows(LibraryUserNotFoundException.class, ()->{
            libraryUserService.update(updateLibraryUser, updateLibraryUser.getUserId());
        });

        //Then
        verify(libraryUserRepository, times(1)).findById(updateLibraryUser.getUserId());
    }

    @Test
    void testDeleteLibraryUserSuccess() {
        //Given
        var libraryUser = new LibraryUser();
        libraryUser.setUserId(1);
        libraryUser.setUserName("user1");
        libraryUser.setPassword("password1");
        libraryUser.setEnabled(true);
        libraryUser.setRoles("admin user");

        //first find and then delete
        given(libraryUserRepository.findById(libraryUser.getUserId())).willReturn(Optional.of(libraryUser));
        doNothing().when(libraryUserRepository).deleteById(1);

        //When
        libraryUserService.delete(libraryUser.getUserId());

        //Then
        verify(libraryUserRepository, times(1)).findById(libraryUser.getUserId());
    }
}