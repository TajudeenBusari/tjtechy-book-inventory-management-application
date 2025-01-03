package com.tjtechy.tjtechyinventorymanagementsept2024.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto.AuthorDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.ChatClient;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatRequest;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatResponse;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.Choice;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.Message;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.BookNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;


import java.util.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
/**
 * this will override the active profile in application.yml file.
 * Irrespective of the active profile, the test will only run using the
 * h2-database
 *
 */
@ActiveProfiles(value = "h2-database")
class BookServiceTest {

    @Mock
    BookRepository bookRepository;
    @Mock
    UUID uuid;

    @InjectMocks
    BookService bookService;

    @Mock
    ChatClient chatClient;

    List<Book> books;

    @BeforeEach
    void setUp() {
        books = new ArrayList<>(); //initialize array with null

        var book1 = new Book();
        book1.setISBN(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"));
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
        this.books.add(book1); //add book1 to list books

        var book2 = new Book();
        book2.setISBN(UUID.fromString("9ab93081-738b-4acb-b0d9-9113c59a38f4"));
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
        this.books.add(book2); //add book2 to list books

        var book3 = new Book();
        book3.setISBN(UUID.fromString("76d452b3-add8-4980-9ae9-0750ccd524cf"));
        book3.setTitle("Book 3");
        book3.setPublisher("person 3");
        book3.setPublicationDate(new Date(1726422581000L));
        book3.setGenre("some book3 genre");
        book3.setEdition("some book3 edition");
        book3.setLanguage("some book3 language");
        book3.setPages(100);
        book3.setDescription("some book2 description");
       // book3.setCoverImageUrl("www.imagebook3.png");
        book3.setPrice(1000.00);
        book3.setQuantity("5");
        this.books.add(book3); //add book3 to list books

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindByIsbnSuccess() {
        //LocalDate localDate = LocalDate.of(2024, 9, 18);
        //https://www.epochconverter.com/
        Date date = new Date(1726680002000L);
        //Given. Arrange inputs and targets.Define the behaviour of mock object bookRepository
        var book = new Book();
        //uuid generator. https://www.uuidgenerator.net/version4
        book.setISBN(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"));
        book.setTitle("some book1 title");
        book.setPublisher("person 1");
        book.setPublicationDate(date);
        book.setGenre("some book1 genre");
        book.setEdition("some book1 edition");
        book.setLanguage("some book1 language");
        book.setPages(100);
        book.setDescription("some book1 description");
        //book.setCoverImageUrl("www.imagebook1.png");
        book.setPrice(1000.00);
        book.setQuantity("3");

        Author author = new Author();
        author.setAuthorId(10000L);
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setEmail("john@doe.com");
        author.setBiography("some author biography");

        book.setOwner(author);

        //findById returns Optional-->check from the Jpa Repo doc
        given(bookRepository.findById(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002")))
                .willReturn(Optional.of(book));//defines the behaviour of mock object

        //When. Act on the target behaviour
        Book returnedBook = bookService.findByIsbn(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"));

        //Then. Assert
        assertThat(returnedBook.getISBN()).isEqualTo(book.getISBN());
        assertThat(returnedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(returnedBook.getPublisher()).isEqualTo(book.getPublisher());
        assertThat(returnedBook.getPublicationDate()).isEqualTo(book.getPublicationDate());
        assertThat(returnedBook.getGenre()).isEqualTo(book.getGenre());
        assertThat(returnedBook.getEdition()).isEqualTo(book.getEdition());
        assertThat(returnedBook.getLanguage()).isEqualTo(book.getLanguage());
        assertThat(returnedBook.getPages()).isEqualTo(book.getPages());
        assertThat(returnedBook.getDescription()).isEqualTo(book.getDescription());
        assertThat(returnedBook.getPrice()).isEqualTo(book.getPrice());
        assertThat(returnedBook.getQuantity()).isEqualTo(book.getQuantity());
        verify(bookRepository, times(1)).findById(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"));

    }

    @Test
    void testFindByIsbnNotFound() {
        //Given
        given(bookRepository.findById(Mockito.any())).willReturn(Optional.empty());

        //When
        Throwable thrown = catchThrowable(()->{
            Book returnedBook = bookService.findByIsbn(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"));
        });

        //Then
        assertThat(thrown)
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Could not find book with isbn 64534f0e-7525-11ef-b864-0242ac120002");
        verify(bookRepository, times(1)).findById(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"));
    }

    @Test
    void testFindAllSuccess() {
        //Given
        given(bookRepository.findAll()).willReturn(this.books);

        //When
        List<Book> actualBooks = bookService.findAll();


        //Then
        assertThat(actualBooks.size()).isEqualTo(this.books.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testSaveSuccess() {
        //Given
        var newBook = new Book();
        newBook.setTitle("new title1");
        newBook.setPublisher("new publisher1");
        newBook.setGenre("new genre1");
        newBook.setEdition("new edition1");
        newBook.setLanguage("new language1");
        newBook.setPages(100);
        newBook.setDescription("new description1");
        newBook.setPrice(1000.00);
        newBook.setQuantity("3");
        newBook.setPublicationDate(new Date(1726680002000L));

        given(bookRepository.save(newBook)).willReturn(newBook);

        //When
        Book savedBook = bookService.save(newBook);

        //Then
        assertThat(savedBook.getISBN()).isEqualTo(newBook.getISBN());
        assertThat(savedBook.getPublisher()).isEqualTo(newBook.getPublisher());
        assertThat(savedBook.getGenre()).isEqualTo(newBook.getGenre());
        assertThat(savedBook.getEdition()).isEqualTo(newBook.getEdition());
        assertThat(savedBook.getLanguage()).isEqualTo(newBook.getLanguage());
        assertThat(savedBook.getPages()).isEqualTo(newBook.getPages());
        assertThat(savedBook.getDescription()).isEqualTo(newBook.getDescription());
        assertThat(savedBook.getPrice()).isEqualTo(newBook.getPrice());
        assertThat(savedBook.getQuantity()).isEqualTo(newBook.getQuantity());
        assertThat(savedBook.getPublicationDate()).isEqualTo(newBook.getPublicationDate());

        verify(bookRepository, times(1)).save(newBook);
    }
    @Test
    void testUpdateBookSuccess() {
        //Given
        //old book to be updated
        var oldBook = new Book();
        oldBook.setISBN(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"));
        oldBook.setTitle("title1");
        oldBook.setPublisher("publisher1");
        oldBook.setGenre("genre1");
        oldBook.setEdition("edition1");
        oldBook.setLanguage("language1");
        oldBook.setPages(100);
        oldBook.setDescription("description1");
        oldBook.setPrice(1000.00);
        oldBook.setQuantity("3");
        oldBook.setPublicationDate(new Date(1726680002000L));

        //book info provided by front end
        var update = new Book();
        update.setISBN(UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"));
        update.setTitle("updated title1");
        update.setPublisher("updated publisher1");
        update.setGenre("updated genre1");
        update.setEdition("edition1");
        update.setLanguage("language1");
        update.setPages(100);
        update.setDescription("description1");
        update.setPrice(1000.00);
        update.setQuantity("3");
        update.setPublicationDate(new Date(1726680002000L));

        //first find the existing book and save
        given(bookRepository.findById(oldBook.getISBN())).willReturn(Optional.of(oldBook));
        given(bookRepository.save(oldBook)).willReturn(oldBook);

        //When
        // then update title, publisher, genre
        Book updatedBook = bookService.update(update, oldBook.getISBN());

        //Then
        assertThat(updatedBook.getISBN()).isEqualTo(update.getISBN());
        assertThat(updatedBook.getTitle()).isEqualTo(update.getTitle());
        assertThat(updatedBook.getPublisher()).isEqualTo(update.getPublisher());
        assertThat(updatedBook.getGenre()).isEqualTo(update.getGenre());
        verify(bookRepository, times(1)).findById(oldBook.getISBN());
        verify(bookRepository, times(1)).save(oldBook);
    }

    @Test
    void testUpdateBookNotFound() {

        //Given
        //because the old does not even exist
        var update = new Book();
        update.setTitle("updated title1");
        update.setPublisher("updated publisher1");
        update.setGenre("updated genre1");
        update.setEdition("edition1");
        update.setLanguage("language1");
        update.setPages(100);
        update.setDescription("description1");
        update.setPrice(1000.00);
        update.setQuantity("3");
        update.setPublicationDate(new Date(1726680002000L));

        given(bookRepository.findById(update.getISBN())).willReturn(Optional.empty());

        //When
        assertThrows(BookNotFoundException.class, () -> bookService.update(update, update.getISBN()));

        //Then
        verify(bookRepository, times(1)).findById(update.getISBN());
    }

    @Test
    void testDeleteBookSuccess() {
        //Given
        UUID uuid = UUID.randomUUID();
        Date date = new Date(1726680002000L);

        var book = new Book();
        book.setISBN(uuid);
        book.setTitle("title1");
        book.setPublisher("publisher1");
        book.setGenre("genre1");
        book.setEdition("edition1");
        book.setLanguage("language1");
        book.setPages(100);
        book.setDescription("description1");
        book.setPrice(1000.00);
        book.setQuantity("3");
        book.setPublicationDate(date);

        //mock behavior of the findById and deleteById method of the Repository because we have find and then delete
        given(bookRepository.findById(book.getISBN())).willReturn(Optional.of(book));
        doNothing().when(bookRepository).deleteById(uuid); //since delete of the repo returns void

        //When
        bookService.delete(book.getISBN());

        //Then
        verify(bookRepository, times(1)).deleteById(book.getISBN());
    }

    @Test
    void testDeleteBookNotFound() {
        UUID uuid = UUID.randomUUID();
        //mock behavior of the findById and there is no need to mock the behaviour of the deleteById
        given(bookRepository.findById(uuid)).willReturn(Optional.empty());


        //When
        assertThrows(BookNotFoundException.class, () -> {
            bookService.delete(uuid);
        });

        //Then
        verify(bookRepository, times(1)).findById(uuid);
    }

    @Test
    void testSummarizeSuccess() throws JsonProcessingException {
        //Given
        var authorDto = new AuthorDto(
                10000L,
                "John",
                "Doe",
                "joh@doe.com",
                "some author biography",
                3);
        List <BookDto> bookDtos = List.of(
                new BookDto(
                        UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002"),
                        "title1",
                        new Date(1726680002000L),
                        "publisher1",
                        "genre1",
                        "edition1",
                        "language1",
                        100,
                        "description1",
                        1000.00,
                        "3",
                        authorDto
                ),
                new BookDto(
                        UUID.fromString("9ab93081-738b-4acb-b0d9-9113c59a38f4"),
                        "title2",
                        new Date(1726163381000L),
                        "publisher2",
                        "genre2",
                        "edition2",
                        "language2",
                        100,
                        "description2",
                        1000.00,
                        "4",
                        authorDto
                ),
                new BookDto(
                        UUID.fromString("76d452b3-add8-4980-9ae9-0750ccd524cf"),
                        "title3",
                        new Date(1726422581000L),
                        "publisher3",
                        "genre3",
                        "edition3",
                        "language3",
                        100,
                        "description3",
                        1000.00,
                        "5",
                        authorDto
                )
        );

        ObjectMapper objectMapper = new ObjectMapper();
        var jsonArray = objectMapper.writeValueAsString(bookDtos);


        //define the behaviour of the chatClient
        var messages = List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of books, each book's description and the publisher information. Please don't include the statement, The JSON array is as follows:"),
                new Message("user", jsonArray)
        );
        var chatRequest = new ChatRequest("gpt-4", messages);
        var chatResponse = new ChatResponse(List.of(
                new Choice(0, new Message("assistant", "The summary is: There are 3 books. The first book is titled title1, published by publisher1. It has 100 pages and is described as description1. The second book is titled title2, published by publisher2. It has 100 pages and is described as description2. The third book is titled title3, published by publisher3. It has 100 pages and is described as description3."))));
        given(this.chatClient.generate(chatRequest)).willReturn(chatResponse);

        //When
        String summary = bookService.summarize(bookDtos);

        //Then
        assertThat(summary)
                .isEqualTo("The summary is: There are 3 books. The first book is titled title1, published by publisher1. It has 100 pages and is described as description1. The second book is titled title2, published by publisher2. It has 100 pages and is described as description2. The third book is titled title3, published by publisher3. It has 100 pages and is described as description3.");
        verify(chatClient, times(1)).generate(chatRequest);
    }

}