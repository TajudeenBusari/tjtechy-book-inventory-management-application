package com.tjtechy.tjtechyinventorymanagementsept2024.user.service;

import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.LibraryUserNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.MyUserPrincipal;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.repository.LibraryUserRepository;
import jakarta.transaction.Transactional;

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

    public LibraryUserService(LibraryUserRepository libraryUserRepository, PasswordEncoder passwordEncoder) {

        this.libraryUserRepository = libraryUserRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_Admin"))) {
            foundlibraryUser.setUserName(updateLibraryUser.getUserName());

        } else {
        //if user is an admin, then the user can update username, roles and enabled status.
        foundlibraryUser.setUserName(updateLibraryUser.getUserName());
        foundlibraryUser.setRoles(updateLibraryUser.getRoles());
        foundlibraryUser.setEnabled(updateLibraryUser.isEnabled());
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
}
