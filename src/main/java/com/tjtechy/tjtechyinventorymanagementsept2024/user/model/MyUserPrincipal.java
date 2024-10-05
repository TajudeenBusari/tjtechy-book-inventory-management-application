package com.tjtechy.tjtechyinventorymanagementsept2024.user.model;

import ch.qos.logback.core.util.StringUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MyUserPrincipal implements UserDetails {

    /*we can use this class to wrap the LibraryUser returned by the repo class
    *
    * */

    private final LibraryUser libraryUser;

    public MyUserPrincipal(LibraryUser libraryUser) {
        this.libraryUser = libraryUser;
    }


    /**
     * Convert a user's roles from space-delimited string to a list of SimpleGrantedAuthority objects.
     * E.g., john's roles are stored in a string like "admin user moderator", we need to convert it to a list of GrantedAuthority.
     * Before conversion, we need to add this "ROLE_" prefix to each role name.
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Arrays.stream(StringUtils.tokenizeToStringArray(this.libraryUser.getRoles(), " "))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

    }

    /**
     * @return
     */
    @Override
    public String getPassword() {
        return this.libraryUser.getPassword();
    }

    /**
     * @return
     */
    @Override
    public String getUsername() {
        return this.libraryUser.getUserName();
    }

    /**
     * @return
     * we don't have this, so we return true
     * else authentication will fail even if you right credentials
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @return
     *  we don't have this, so we return true
     *  else authentication will fail even if you right credentials
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @return
     *  we don't have this, so we return true
     *  else authentication will fail even if you right credentials
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public boolean isEnabled() {
        return this.libraryUser.isEnabled();
    }

    //a getter for LibraryUser, we need to get it from the principal object
    public LibraryUser getLibraryUser() {
        return libraryUser;
    }
}
