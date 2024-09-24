package com.tjtechy.tjtechyinventorymanagementsept2024.book.controller;

import com.tjtechy.tjtechyinventorymanagementsept2024.book.converter.BookDtoToBookConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.converter.BookToBookDtoConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.service.BookService;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.Result;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.endpoint.base-url}/books")
public class BookController {

    private final BookService bookService;

    private final BookToBookDtoConverter bookToBookDtoConverter;

    private final BookDtoToBookConverter bookDtoToBookConverter;

    public BookController(BookService bookService, BookToBookDtoConverter bookToBookDtoConverter, BookDtoToBookConverter bookDtoToBookConverter) {
        this.bookService = bookService;
        this.bookToBookDtoConverter = bookToBookDtoConverter;
        this.bookDtoToBookConverter = bookDtoToBookConverter;
    }

    @GetMapping("/{bookIsbn}")
    public Result findBookByISBN(@PathVariable UUID bookIsbn){

        Book foundBook = this.bookService.findByIsbn(bookIsbn);

        //Convert found book to dto
        var bookDto = this.bookToBookDtoConverter.convert(foundBook);

        return new Result(true, StatusCode.SUCCESS, "Find One Success", bookDto);

        /*Behind the scene before the spring mvc returns the Result to the client,
        *the spring mvc will automatically serialize the Result object into a json
        * string and send the json string back to the client
        *
        *
        * */
    }

    @GetMapping
    public Result findAllBooks(){

        List<Book> foundBooks = this.bookService.findAll();

        //convert found books to Dtos
        List<BookDto> booksDto = foundBooks
                .stream()
                .map(foundBook -> this.bookToBookDtoConverter.convert(foundBook))
                .collect(Collectors.toList());

        return new Result(true, StatusCode.SUCCESS, "Find All Success", booksDto);
    }

    @PostMapping
    public Result addBook(@Valid @RequestBody BookDto bookDto){
        
        //convert BookDto to Book
        Book newBook = this.bookDtoToBookConverter.convert(bookDto);

        Book savedBook = this.bookService.save(newBook);

        //convert the savedBook to the Dto
        BookDto savedBookDto = this.bookToBookDtoConverter.convert(savedBook);

        return new Result(true, StatusCode.SUCCESS, "Add Success", savedBookDto);
    }

    @PutMapping("/{bookIsbn}")
    public Result updateBook(@Valid @RequestBody BookDto bookDto, @PathVariable UUID bookIsbn){
        //convert to Book
        Book update = this.bookDtoToBookConverter.convert(bookDto);
        Book updatedBook = this.bookService.update(update, bookIsbn);
        //convert to Dto
        BookDto updatedBookDto = this.bookToBookDtoConverter.convert(updatedBook);

        return new Result(true, StatusCode.SUCCESS, "Update Success", updatedBookDto);
    }

    @DeleteMapping("/{bookIsbn}")
    public Result deleteBook(@PathVariable UUID bookIsbn){
        this.bookService.delete(bookIsbn);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");//we use the constructor with 3 params
    }

}
