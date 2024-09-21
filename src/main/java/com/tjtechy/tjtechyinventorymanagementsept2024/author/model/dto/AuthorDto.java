package com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto;

public record AuthorDto(
        Long authorId
        , String firstName,
        String lastName,
        String email,
        String biography,
        Integer numberOfBooks
) {
}
