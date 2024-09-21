package com.tjtechy.tjtechyinventorymanagementsept2024.author.converter;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto.AuthorDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AuthorToAuthorDtoConverter implements Converter<Author, AuthorDto> {
    @Override
    public AuthorDto convert(Author source) {
        var authorDto = new AuthorDto(
                source.getAuthorId(),
                source.getFirstName(),
                source.getLastName(),
                source.getEmail(),
                source.getBiography(),
                source.getNumberOfBooks()
        );
        return authorDto;
    }
}
