package com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto.AuthorDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Date;
import java.util.UUID;

public record BookDto(
        UUID ISBN,  //to add book, you need to manually generate the uuid and include as part of the request--->, not anymore
        @NotEmpty(message = "title is required.")
        String title,
        Date publicationDate,
        @NotEmpty(message = "publisher is required.")
        String publisher,
        @NotEmpty(message = "genre is required.")
        String Genre,
        @NotEmpty(message = "edition is required.")
        String edition,
        @NotEmpty(message = "language is required.")
        String language,
        int pages,
        @NotEmpty(message = "description is required.")
        String description,

        @NotNull(message = "Price is required")  // Makes sure price cannot be null
        @Positive(message = "Price must be positive") // Ensures price is greater than zero
        Double price,
        @NotEmpty(message = "quantity is required.")
        String Quantity,
        AuthorDto owner
) {

}
/*
* In addition to this kind of validation @NotEmpty, you can also define the @Length-->min and max length or
* @Pattern-->regular expressions (regex)
* */