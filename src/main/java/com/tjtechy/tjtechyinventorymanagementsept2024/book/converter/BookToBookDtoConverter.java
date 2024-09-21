package com.tjtechy.tjtechyinventorymanagementsept2024.book.converter;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.converter.AuthorToAuthorDtoConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BookToBookDtoConverter implements Converter<Book, BookDto> {

    private final AuthorToAuthorDtoConverter authorToAuthorDtoConverter;
    public BookToBookDtoConverter(AuthorToAuthorDtoConverter authorToAuthorDtoConverter) {
        this.authorToAuthorDtoConverter = authorToAuthorDtoConverter;
    }
    @Override
    public BookDto convert(Book source) {
        var bookDto = new BookDto(
                source.getISBN(),
                source.getTitle(),
                source.getPublicationDate(),
                source.getPublisher(),
                source.getGenre(),
                source.getEdition(),
                source.getLanguage(),
                source.getPages(),
               // source.getCoverImageUrl(),
                source.getDescription(),
                source.getPrice(),
                source.getQuantity(),
                source.getOwner() != null
                        ? this.authorToAuthorDtoConverter.convert(source.getOwner())
                        : null

        );

        return bookDto;
    }
}
