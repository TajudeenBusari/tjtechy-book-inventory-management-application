package com.tjtechy.tjtechyinventorymanagementsept2024.book.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.converter.BookDtoToBookConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.converter.BookToBookDtoConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.service.BookService;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.Result;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    //create our own custom metrics, for example to know which book is more popular
    private final MeterRegistry meterRegistry;

    public BookController(BookService bookService, BookToBookDtoConverter bookToBookDtoConverter, BookDtoToBookConverter bookDtoToBookConverter, MeterRegistry meterRegistry) {
        this.bookService = bookService;
        this.bookToBookDtoConverter = bookToBookDtoConverter;
        this.bookDtoToBookConverter = bookDtoToBookConverter;
      this.meterRegistry = meterRegistry;
    }

    @GetMapping("/{bookIsbn}")
    public Result findBookByISBN(@PathVariable UUID bookIsbn){

        Book foundBook = this.bookService.findByIsbn(bookIsbn);
        meterRegistry.counter("book.isbn" + bookIsbn).increment(); //returns a new counter or an existing counter
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
    public Result findAllBooks(Pageable pageable){

        Page<Book> bookPage = this.bookService.findAll(pageable);

        //convert found booksDtoPage to Dtos
        Page<BookDto> booksDtoPage = bookPage

                .map(foundBook -> this.bookToBookDtoConverter.convert(foundBook));

        return new Result(true, StatusCode.SUCCESS, "Find All Success", booksDtoPage);
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
    //refactor your code to have a separate dto class for update that will mot make any field required
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

    @GetMapping("/summary")
    public Result summarizeBooks() throws JsonProcessingException {
        //get the total number of books from the database
        List<Book> allBooks = this.bookService.findAll();
        //convert the books to dtos
        List<BookDto> bookDtos = allBooks.stream()
                .map(book -> this.bookToBookDtoConverter.convert(book))
                .collect(Collectors.toList());
        String bookSummary = this.bookService.summarize(bookDtos);

        return new Result(true, StatusCode.SUCCESS, "Summary Success", bookSummary);
    }

}
