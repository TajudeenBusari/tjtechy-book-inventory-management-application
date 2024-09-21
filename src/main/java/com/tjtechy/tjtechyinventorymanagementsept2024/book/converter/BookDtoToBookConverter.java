package com.tjtechy.tjtechyinventorymanagementsept2024.book.converter;

import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BookDtoToBookConverter implements Converter<BookDto, Book> {

    @Override
    public Book convert(BookDto source) {
        var book = new Book();
        book.setISBN(source.ISBN());
        book.setTitle(source.title());
        book.setPublicationDate(source.publicationDate());
        book.setPublisher(source.publisher());
        book.setEdition(source.edition());
        book.setPages(source.pages());
        book.setLanguage(source.language());
        book.setPrice(source.price());
        book.setGenre(source.Genre());
        book.setQuantity(source.Quantity());
        //book.setCoverImageUrl(source.coverImageUrl());
        book.setDescription(source.description());

        return book;
    }
}
