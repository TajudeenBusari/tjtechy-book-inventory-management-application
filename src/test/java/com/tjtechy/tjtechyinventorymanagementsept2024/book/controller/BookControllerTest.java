package com.tjtechy.tjtechyinventorymanagementsept2024.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.controller.AuthorController;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.rediscache.RedisCacheClient;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.BookNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.service.BookService;
import com.tjtechy.tjtechyinventorymanagementsept2024.security.JwtInterceptor;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(BookController.class) //THIS ENSURES THIS TESTS WILL NOT FAIL IF I RUN EVERYTHING AT ONCE IN THE CONTROLLER FOLDER
//@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)//turns off spring security
/**
 * this will override the active profile in application.yml file.
 * Irrespective of the active profile, the test will only run using the
 * h2-database
 *
 */
@ActiveProfiles(value = "h2-database")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean //for controller test, we use MockBean
    BookService bookService;

    /**these RedisCacheClient and JwtInterceptor  are added
     * because redisCache client was introduced in the project.
     * Without them controller unit tests are failing.
     *
     *
     * */
    @MockitoBean
    RedisCacheClient redisCacheClient;

    @Autowired
    JwtInterceptor jwtInterceptor;

    @Autowired
    ObjectMapper objectMapper; //from fasterxml.jackson.databind

    @MockitoBean
    MeterRegistry meterRegistry;

    //let's create a list of Books
    List<Book> books;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach //GETS CALL BEFORE ANY METHOD TEST IS CALLED
    void setUp() {
        books = new ArrayList<>();

        var book1 = new Book();
        var bookIsbn1 = UUID.randomUUID();
        book1.setISBN(bookIsbn1);
        book1.setTitle("Book 1");
        book1.setPublisher("person 1");
        book1.setPublicationDate(new Date(1726680002000L));
        book1.setGenre("some book1 genre");
        book1.setEdition("some book1 edition");
        book1.setLanguage("some book1 language");
        book1.setPages(100);
        book1.setDescription("some book1 description");
        book1.setPrice(1000.00);
        book1.setQuantity("3");
        this.books.add(book1);

        var book2 = new Book();
        var bookIsbn2 = UUID.fromString("9ab93081-738b-4acb-b0d9-9113c59a38f4");
        book2.setISBN(bookIsbn2);
        book2.setTitle("Book 2");
        book2.setPublisher("person 2");
        book2.setPublicationDate(new Date(1726163381000L));
        book2.setGenre("some book2 genre");
        book2.setEdition("some book2 edition");
        book2.setLanguage("some book2 language");
        book2.setPages(100);
        book2.setDescription("some book2 description");
        book2.setPrice(1000.00);
        book2.setQuantity("4");
        this.books.add(book2);

        var book3 = new Book();
        var bookIsbn3 = UUID.fromString("76d452b3-add8-4980-9ae9-0750ccd524cf");
        book3.setISBN(bookIsbn3);
        book3.setTitle("Book 3");
        book3.setPublisher("person 3");
        book3.setPublicationDate(new Date(1726422581000L));
        book3.setGenre("some book3 genre");
        book3.setEdition("some book3 edition");
        book3.setLanguage("some book3 language");
        book3.setPages(100);
        book3.setDescription("some book2 description");
        book3.setPrice(1000.00);
        book3.setQuantity("5");
        this.books.add(book3);

        var book4 = new Book();
        var bookIsbn4 = UUID.fromString("058949bf-949f-4406-8f3f-76265cff8006");
        book4.setISBN(bookIsbn4);
        book4.setTitle("Book 4");
        book4.setPublisher("person 4");
        book4.setPublicationDate(new Date(1726249781000L));
        book4.setGenre("some book4 genre");
        book4.setEdition("some book4 edition");
        book4.setLanguage("some book4 language");
        book4.setPages(100);
        book4.setDescription("some book2 description");
        book4.setPrice(100.00);
        book4.setQuantity("6");
        this.books.add(book4);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindBookByISBNSuccess() throws Exception {
        //Given

        given(this.bookService.findByIsbn(books.get(0).getISBN())).willReturn(books.get(0));

        var bookIsbn = books.get(0).getISBN();

        Counter mockCounter = mock(Counter.class);
        given(meterRegistry.counter("book.isbn" + bookIsbn)).willReturn(mockCounter);

        //When and Then
        this.mockMvc.perform(get(this.baseUrl+ "/books/{bookIsbn}", bookIsbn).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.ISBN").value(bookIsbn.toString()))
                .andExpect(jsonPath("$.data.title").value("Book 1"));

        // Verify that the service and counter were called
        verify(this.bookService).findByIsbn(bookIsbn);
        verify(mockCounter).increment();
    }

    @Test
    void testFindBookByISBNNotFound() throws Exception {
        //Given

        //GET THE UUID into a variable
//        String baseurl = ""
        UUID bookIsbn = UUID.fromString("058949bf-949f-4406-8f3f-76265cff8006");

        given(this.bookService.findByIsbn(UUID.fromString("058949bf-949f-4406-8f3f-76265cff8006")))
                .willThrow(new BookNotFoundException(bookIsbn));

        //When and Then
        this.mockMvc.perform(get(this.baseUrl + "/books/{bookIsbn}", bookIsbn).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find book with isbn " + bookIsbn))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllBooksSuccess() throws Exception {
        //Given
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<Book> bookPage = new PageImpl<>(this.books, pageable, this.books.size());

        given(this.bookService.findAll(Mockito.any(Pageable.class))).willReturn(bookPage);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "20");
        //you can add more request params here

        //When and Then
        this.mockMvc.perform(get(this.baseUrl + "/books").accept(MediaType.APPLICATION_JSON).params(requestParams))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(this.books.size())))
                .andExpect(jsonPath("$.data.content[0].ISBN").value(this.books.get(0).getISBN().toString()))
                .andExpect(jsonPath("$.data.content[0].title").value("Book 1"))
                .andExpect(jsonPath("$.data.content[0].publisher").value("person 1"))
                .andExpect(jsonPath("$.data.content[1].ISBN").value(this.books.get(1).getISBN().toString()))
                .andExpect(jsonPath("$.data.content[1].title").value("Book 2"))
                .andExpect(jsonPath("$.data.content[1].publisher").value("person 2"));
    }

    @Test
    void testAddBookSuccess() throws Exception {

        //Given
        Date date = new Date(1726680002000L);
        var bookDto = new BookDto(
                null,
                "new title1",
                date,
                "new publisher1",
                "new genre1",
                "new edition1",
                "new language1",
                100,
                "new description1",
                100.00,
                "3",
                null

                );

        String json = this.objectMapper.writeValueAsString(bookDto);

        //data the service layer will return
        var savedBook = new Book();
        savedBook.setISBN(UUID.fromString("058949bf-949f-4406-8f3f-76265cff8006"));
        savedBook.setTitle("new title1");
        savedBook.setPublisher("new publisher1");
        savedBook.setGenre("new genre1");
        savedBook.setEdition("new edition1");
        savedBook.setLanguage("new language1");
        savedBook.setPages(100);
        savedBook.setDescription("new description1");
        savedBook.setPrice(1000.00);
        savedBook.setQuantity("3");
        savedBook.setPublicationDate(date);

        given(this.bookService.save(Mockito.any(Book.class))).willReturn(savedBook);

        //When and //Then
        this.mockMvc.perform(post(this.baseUrl + "/books").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.ISBN").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value(savedBook.getTitle()))
                .andExpect(jsonPath("$.data.publisher").value(savedBook.getPublisher()))
                .andExpect(jsonPath("$.data.edition").value(savedBook.getEdition()))
                .andExpect(jsonPath("$.data.language").value(savedBook.getLanguage()))
                .andExpect(jsonPath("$.data.pages").value(savedBook.getPages()))
                .andExpect(jsonPath("$.data.description").value(savedBook.getDescription()))
                .andExpect(jsonPath("$.data.price").value(savedBook.getPrice()))
                .andExpect(jsonPath("$.data.Quantity").value(savedBook.getQuantity()))
                .andExpect(jsonPath("$.data.publicationDate").exists());

    }

    @Test
    void testUpdateBookSuccess() throws Exception {
        //Given
        Date date = new Date(1726680002000L);
        UUID bookId = UUID.randomUUID();
        var bookDto = new BookDto(
                bookId,
                "new title1",
                date,
                "new publisher1",
                "new genre1",
                "new edition1",
                "new language1",
                100,
                "new description1",
                100.00,
                "3",
                null

        );

        String json = this.objectMapper.writeValueAsString(bookDto);

        var updatedBook = new Book();
        updatedBook.setISBN(bookId);
        updatedBook.setTitle("update new title1");
        updatedBook.setPublisher("update new publisher1");
        updatedBook.setGenre("update new genre1");
        updatedBook.setEdition("new edition1");
        updatedBook.setLanguage("new language1");
        updatedBook.setPages(100);
        updatedBook.setDescription("new description1");
        updatedBook.setPrice(1000.00);
        updatedBook.setQuantity("3");
        updatedBook.setPublicationDate(date);

        given(this.bookService.update(Mockito.any(Book.class), eq(bookId))).willReturn(updatedBook);



        //When and Then
        this.mockMvc.perform(put(this.baseUrl + "/books/{bookId}", bookId).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.ISBN").value(updatedBook.getISBN().toString()))
                .andExpect(jsonPath("$.data.title").value(updatedBook.getTitle()))
                .andExpect(jsonPath("$.data.publisher").value(updatedBook.getPublisher()))
                .andExpect(jsonPath("$.data.edition").value(updatedBook.getEdition()))
                .andExpect(jsonPath("$.data.language").value(updatedBook.getLanguage()))
                .andExpect(jsonPath("$.data.pages").value(updatedBook.getPages()))
                .andExpect(jsonPath("$.data.description").value(updatedBook.getDescription()))
                .andExpect(jsonPath("$.data.price").value(updatedBook.getPrice()))
                .andExpect(jsonPath("$.data.Quantity").value(updatedBook.getQuantity()))
                .andExpect(jsonPath("$.data.publicationDate").exists());

    }

    @Test
    void testUpdateBookIsbnNotFound() throws Exception {
        UUID bookId = UUID.randomUUID();
        Date date = new Date(1726680002000L);


        var bookDto = new BookDto(
                bookId,
                "new title1",
                date,
                "new publisher1",
                "new genre1",
                "new edition1",
                "new language1",
                100,
                "new description1",
                100.00,
                "3",
                null
        );

        String json = this.objectMapper.writeValueAsString(bookDto);
        given(this.bookService.update(Mockito.any(Book.class), eq(bookId))).willThrow(new BookNotFoundException(bookId));


        //When and Then
        this.mockMvc.perform(put(this.baseUrl + "/books/{bookId}", bookId).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find book with isbn " + bookId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteBookSuccess() throws Exception {
        //Given
        UUID bookId = UUID.randomUUID();
        doNothing().when(this.bookService).delete(bookId);

        //When and Then
        this.mockMvc.perform(delete(this.baseUrl + "/books/{bookId}", bookId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteBookNotFound() throws Exception {
        //Given
        UUID bookId = UUID.randomUUID();
        doThrow(new BookNotFoundException(bookId)).when(this.bookService).delete(bookId);

        //When and Then
        this.mockMvc.perform(delete(this.baseUrl + "/books/{bookId}", bookId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find book with isbn " + bookId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testSummarizeBookSuccess() throws Exception {
        //Given
        given(this.bookService.summarize(Mockito.anyList())).willReturn("The summary includes 4 books with their authors");

        //When and Then
        this.mockMvc.perform(get(this.baseUrl + "/books/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Summary Success"))
                .andExpect(jsonPath("$.data").value("The summary includes 4 books with their authors"));
    }




}