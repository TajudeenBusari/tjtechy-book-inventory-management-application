package com.tjtechy.tjtechyinventorymanagementsept2024.user.service;

import com.tjtechy.tjtechyinventorymanagementsept2024.client.rediscache.RedisCacheClient;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.PasswordChangeIllegalArgumentException;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.LibraryUserNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.MyUserPrincipal;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.repository.LibraryUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
/**
 * this will override the active profile in application.yml file.
 * Irrespective of the active profile, the test will only run using the
 * h2-database
 *
 */
@ActiveProfiles(value = "h2-database")
class LibraryUserServiceTest {

    @Mock
    LibraryUserRepository libraryUserRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RedisCacheClient redisCacheClient;

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

        given(this.passwordEncoder.encode(newLibraryUser.getPassword())).willReturn("Encoded password");
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
    void testUpdateLibraryUserByAdminSuccess() {
        //Given
        var oldLibraryUser = new LibraryUser();
        oldLibraryUser.setUserId(2);
        oldLibraryUser.setUserName("eric");
        oldLibraryUser.setPassword("654321");
        oldLibraryUser.setEnabled(true);
        oldLibraryUser.setRoles("user");

        var updateLibraryUser = new LibraryUser();
        oldLibraryUser.setUserName("eric-update"); //update username
        updateLibraryUser.setPassword("654321");
        updateLibraryUser.setEnabled(true);
        oldLibraryUser.setRoles("Admin user"); //update role

        //first find if user exists and save
        given(libraryUserRepository.findById(oldLibraryUser.getUserId())).willReturn(Optional.of(oldLibraryUser));
        given(libraryUserRepository.save(oldLibraryUser)).willReturn(oldLibraryUser);

        //create a fake libraryUser that will do the update and set role to Admin
        var libraryUser = new LibraryUser();
        libraryUser.setRoles("Admin");
        var myUserPrincipal = new MyUserPrincipal(libraryUser);

        //create a fake security context
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);


        //When
        LibraryUser updatedLibraryUser = libraryUserService.update(updateLibraryUser, oldLibraryUser.getUserId());

        //then
        assertThat(updatedLibraryUser.getUserId()).isEqualTo(oldLibraryUser.getUserId());
        assertThat(updatedLibraryUser.getUserName()).isEqualTo(updateLibraryUser.getUserName());
        assertThat(updatedLibraryUser.getPassword()).isEqualTo(updateLibraryUser.getPassword());
        assertThat(updatedLibraryUser.isEnabled()).isEqualTo(updateLibraryUser.isEnabled());
        assertThat(updatedLibraryUser.getRoles()).isEqualTo(updateLibraryUser.getRoles());
        verify(libraryUserRepository, times(1)).findById(oldLibraryUser.getUserId());
    }

    @Test
    void testUpdateLibraryUserByUserSuccess() {
        //Given
        var oldLibraryUser = new LibraryUser();
        oldLibraryUser.setUserId(2);
        oldLibraryUser.setUserName("eric");
        oldLibraryUser.setPassword("654321");
        oldLibraryUser.setEnabled(true);
        oldLibraryUser.setRoles("user");

        var updateLibraryUser = new LibraryUser();
        oldLibraryUser.setUserName("eric-update"); //update username
        updateLibraryUser.setPassword("654321");
        updateLibraryUser.setEnabled(true);
        updateLibraryUser.setRoles("user"); //update role

        //first find if user exists and save
        given(libraryUserRepository.findById(oldLibraryUser.getUserId())).willReturn(Optional.of(oldLibraryUser));
        given(libraryUserRepository.save(oldLibraryUser)).willReturn(oldLibraryUser);

        //create a fake libraryUser that will do the update and set role to Admin
        var libraryUser = new LibraryUser();
        libraryUser.setRoles("user");
        var myUserPrincipal = new MyUserPrincipal(libraryUser);

        //create a fake security context
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        //When
        LibraryUser updatedLibraryUser = libraryUserService.update(updateLibraryUser, oldLibraryUser.getUserId());

        //Then

        assertThat(updatedLibraryUser.getUserName()).isEqualTo(updateLibraryUser.getUserName());
        assertThat(updatedLibraryUser.getPassword()).isEqualTo(updateLibraryUser.getPassword());
        assertThat(updatedLibraryUser.isEnabled()).isEqualTo(updateLibraryUser.isEnabled());
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

    @Test
    void testPasswordChangeSuccess(){
        //Given
        var libraryUser = new LibraryUser();
        libraryUser.setUserId(2);
        libraryUser.setPassword("oldEncryptedPassword");

        //mocks
        given(libraryUserRepository.findById(libraryUser.getUserId())).willReturn(Optional.of(libraryUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(passwordEncoder.encode(anyString())).willReturn("newEncryptedPassword");
        given(libraryUserRepository.save(libraryUser)).willReturn(libraryUser);
        doNothing().when(this.redisCacheClient).delete(anyString());

        //When
        libraryUserService.changePassword(libraryUser.getUserId(), "unencryptedOldPassword", "Abc12345", "Abc12345");



        //Then
        assertThat(libraryUser.getPassword()).isEqualTo("newEncryptedPassword");
        verify(libraryUserRepository, times(1)).save(libraryUser);

    }

    @Test
    void testChangePasswordOlPasswordIsIncorrect(){

        //Given
        var libraryUser = new LibraryUser();
        libraryUser.setUserId(2);
        libraryUser.setPassword("oldEncryptedPassword");

        //mocks
        given(libraryUserRepository.findById(libraryUser.getUserId())).willReturn(Optional.of(libraryUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false); //old password matches the password in the DB(mocks, not actual DB)

        Exception badCredentialsException = assertThrows(BadCredentialsException.class, () -> {
            //When
            libraryUserService.changePassword(libraryUser.getUserId(), "unencryptedWrongOldPassword", "Abc12345", "Abc12345");
        });


        //Then
        assertThat(badCredentialsException).isInstanceOf(BadCredentialsException.class).hasMessage("Old password is incorrect.");

    }

    @Test
    void testChangePasswordUserNotFound(){
        //Given

        //mocks
        given(libraryUserRepository.findById(2)).willReturn(Optional.empty());


        Exception notFoundException = assertThrows(LibraryUserNotFoundException.class, () -> {
            //When
            libraryUserService.changePassword(2, "unencryptedWrongOldPassword", "Abc12345", "Abc12345");
        });


        //Then
        assertThat(notFoundException).isInstanceOf(LibraryUserNotFoundException.class).hasMessage("Could not find library user with Id 2");

    }

    @Test
    void testChangePasswordNewPasswordDoesNotMatchConfirmNewPassword(){

        //Given
        var libraryUser = new LibraryUser();
        libraryUser.setUserId(2);
        libraryUser.setPassword("oldEncryptedPassword");

        //mocks
        given(libraryUserRepository.findById(libraryUser.getUserId())).willReturn(Optional.of(libraryUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true); //old password matches the password in the DB(mocks, not actual DB)

        Exception badCredentialsException = assertThrows(PasswordChangeIllegalArgumentException.class, () -> {
            //When
            libraryUserService.changePassword(libraryUser.getUserId(), "unencryptedOldPassword", "Abc12345", "Abc123456");
        });


        //Then
        assertThat(badCredentialsException).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New password and confirm new password do not match.");

    }

    @Test
    void testChangePasswordNewPasswordIsSameAsOldPassword(){

          //Given
          var libraryUser = new LibraryUser();
          libraryUser.setUserId(2);
          libraryUser.setPassword("oldEncryptedPassword");

          //mocks
          given(libraryUserRepository.findById(libraryUser.getUserId())).willReturn(Optional.of(libraryUser));
          given(passwordEncoder.matches(anyString(), anyString())).willReturn(true); //old password matches the password in the DB(mocks, not actual DB)



          Exception badCredentialsException = assertThrows(PasswordChangeIllegalArgumentException.class, () -> {
              //When
              libraryUserService.changePassword(libraryUser.getUserId(), "unencryptedOldPassword", "unencryptedOldPassword", "unencryptedOldPassword");
          });

          //Then
          assertThat(badCredentialsException).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New password must be different from the old password.");

    }

    @Test
    void testChangePasswordNewPasswordDoesNotConformToThePasswordPolicy(){

        //Given
        var libraryUser = new LibraryUser();
        libraryUser.setUserId(2);
        libraryUser.setPassword("oldEncryptedPassword");

        //mocks
        given(libraryUserRepository.findById(libraryUser.getUserId())).willReturn(Optional.of(libraryUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true); //old password matches the password in the DB(mocks, not actual DB)

        Exception badCredentialsException = assertThrows(PasswordChangeIllegalArgumentException.class, () -> {
            //When
            libraryUserService.changePassword(libraryUser.getUserId(), "unencryptedOldPassword", "abc12345", "abc12345");
        });


        //Then
        assertThat(badCredentialsException).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New password must contain at least 8 characters, at least 1 digit, at least 1 lowercase letter, at least 1 uppercase letter.");

    }
}