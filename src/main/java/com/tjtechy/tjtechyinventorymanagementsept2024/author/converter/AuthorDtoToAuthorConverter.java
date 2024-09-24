package com.tjtechy.tjtechyinventorymanagementsept2024.author.converter;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto.AuthorDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AuthorDtoToAuthorConverter implements Converter<AuthorDto, Author> {
    @Override
    public Author convert(AuthorDto source) {
        var author = new Author();
        author.setAuthorId(source.authorId());
        author.setFirstName(source.firstName());
        author.setLastName(source.lastName());
        author.setEmail(source.email());
        author.setBiography(source.biography());
        return author;
    }

}
