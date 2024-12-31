package com.tjtechy.tjtechyinventorymanagementsept2024.user.service;

import com.tjtechy.tjtechyinventorymanagementsept2024.client.rediscache.RedisCacheClient;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.PasswordChangeIllegalArgumentException;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.LibraryUserNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.MyUserPrincipal;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.repository.LibraryUserRepository;
import jakarta.transaction.Transactional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class
LibraryUserService implements UserDetailsService {

    private final LibraryUserRepository libraryUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final RedisCacheClient  redisCacheClient;

    public LibraryUserService(LibraryUserRepository libraryUserRepository, PasswordEncoder passwordEncoder, RedisCacheClient redisCacheClient) {

        this.libraryUserRepository = libraryUserRepository;
        this.passwordEncoder = passwordEncoder;
      this.redisCacheClient = redisCacheClient;
    }

    public LibraryUser save(LibraryUser newLibraryUser) {

        //We NEED to encode plain text password before saving to the DB! TODO
        newLibraryUser.setPassword(this.passwordEncoder.encode(newLibraryUser.getPassword()));
        
        return libraryUserRepository.save(newLibraryUser);
    }

    public LibraryUser findById(Integer userId) {

        return this.libraryUserRepository.findById(userId)
                .orElseThrow(() -> new LibraryUserNotFoundException(userId));
    }

    public List<LibraryUser> findAll() {

        return this.libraryUserRepository.findAll();
    }

    public LibraryUser update(LibraryUser updateLibraryUser, Integer userId) {

        //find first
        LibraryUser foundlibraryUser = this.libraryUserRepository
                .findById(userId)
                .orElseThrow(() -> new LibraryUserNotFoundException(userId));

        //update we are not updating password here
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        //if user is not an admin, then the user can only update her username.
        if (authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_Admin"))) {
            foundlibraryUser.setUserName(updateLibraryUser.getUserName());

        } else {
        //if user is an admin, then the user can update username, roles and enabled status.
        foundlibraryUser.setUserName(updateLibraryUser.getUserName());
        foundlibraryUser.setRoles(updateLibraryUser.getRoles());
        foundlibraryUser.setEnabled(updateLibraryUser.isEnabled());

        //if an admin updates any data, like role, enable or username, revoke the JWT from Redis
            this.redisCacheClient.delete("whitelist:" + userId);

        }

        return this.libraryUserRepository.save(foundlibraryUser);
    }

    public void delete(Integer userId) {
        this.libraryUserRepository.findById(userId)
                .orElseThrow(() -> new LibraryUserNotFoundException(userId));
        this.libraryUserRepository.deleteById(userId);

    }

    /**
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /*//HOW to find user BY username? check user repo
        * since this is not returning LibraryUser but UserDetails(also include username)
        * So we hava to convert LibraryUser to UserDetails, we adapt a design Pattern called Adapter Pattern
        * Lets create a class MyUserPrincipal that will extend the UserDetails in the user model package
        * if found, then map
        * Handle the UsernameNotFoundException in the RestontrollerAdvice
        * */

        return
                this.libraryUserRepository.findByUserName(username)
                .map(libraryUser -> new MyUserPrincipal(libraryUser))
                .orElseThrow(() -> new UsernameNotFoundException("username " + username + " is found."));

    }

    public void changePassword(Integer userId, String oldPassword, String newPassword, String confirmNewPassword) {

        var libraryUser = this.libraryUserRepository.findById(userId)
                .orElseThrow(() -> new LibraryUserNotFoundException(userId));

        //check if old password is not correct, throw an exception
        if (!this.passwordEncoder.matches(oldPassword, libraryUser.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect.");
        }

        //check if new password and confirm new password do not match, throw an exception
        if (!newPassword.equals(confirmNewPassword)) {
            throw new PasswordChangeIllegalArgumentException("New password and confirm new password do not match.");
        }

        //check if new password is the same as the old password, throw an exception
        if (newPassword.equals(oldPassword)) {
            throw new PasswordChangeIllegalArgumentException("New password must be different from the old password.");
        }

        //The new password must contain at least 8 characters, at least 1 digit, at least 1 lowercase letter, at least 1 uppercase letter.
        String passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (!newPassword.matches(passwordPolicy)) {
            throw new PasswordChangeIllegalArgumentException("New password must contain at least 8 characters, at least 1 digit, at least 1 lowercase letter, at least 1 uppercase letter.");
        }

        //Encode and save new password
        libraryUser.setPassword(this.passwordEncoder.encode(newPassword));

        //Revoke this user's current JWT by deleting from RedisCache
        this.redisCacheClient.delete("whitelist:" + userId);

        this.libraryUserRepository.save(libraryUser);


    }
}
