package com.tjtechy.tjtechyinventorymanagementsept2024.user.model.dto;

import jakarta.validation.constraints.NotEmpty;

public record LibraryUserDto (
        Integer userId,
        @NotEmpty(message = "username is required")
        String userName,

        boolean enabled,

        @NotEmpty(message = "roles are required")
        String roles){
}
