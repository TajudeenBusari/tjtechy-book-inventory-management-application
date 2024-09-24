package com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto;

import jakarta.validation.constraints.NotEmpty;

public record AuthorDto(
        Long authorId,
        @NotEmpty(message = "firstName is required")
        String firstName,
        @NotEmpty(message = "lastName is required")
        String lastName,
        @NotEmpty(message = "email is required")
        String email,
        String biography,
        Integer numberOfBooks
) {
}
