package com.tjtechy.tjtechyinventorymanagementsept2024.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
@Entity
@Table(name = "users")
public class LibraryUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) //it will auto generate the identity key and won't use the one in the DbInitializer
    private Integer userId;

    @NotEmpty(message = "username is required")
    private String userName;

    @NotEmpty(message = "password is required")
    private String password;

    //private String email;

    private boolean enabled;

    @NotEmpty(message = "role are required")
    private String roles; //space separated string

    public LibraryUser(Integer userId, String userName, String password, boolean enabled, String roles) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;

        this.enabled = enabled;
        this.roles = roles;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public  String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public  String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public  String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public LibraryUser() {
    }
}
