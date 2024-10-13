package com.tjtechy.tjtechyinventorymanagementsept2024.author.service;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.repository.AuthorRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.repository.BookRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.AuthorNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.BookNotFoundException;
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
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
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
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    List<Author> authorList;

    @BeforeEach
    void setUp() {
        //assuming every author has zero number of books

        authorList = new ArrayList<>();//initialize author with empty array

        var author1 = new Author();
        author1.setAuthorId(1000L);
        author1.setFirstName("John");
        author1.setLastName("Doe");
        author1.setEmail("john@doe.com");
        author1.setBiography("some biography1");
        authorList.add(author1);

        var author2 = new Author();
        author2.setAuthorId(2000L);
        author2.setFirstName("Jane");
        author2.setLastName("Jones");
        author2.setEmail("jane@jones.com");
        author2.setBiography("some biography2");
        authorList.add(author2);

        var author3 = new Author();
        author3.setAuthorId(3000L);
        author3.setFirstName("taju");
        author3.setLastName("lasisi");
        author3.setEmail("taju@lasis.com");
        author3.setBiography("some biography3");
        authorList.add(author3);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findAuthorByIdSuccess() {
        //create any fake author to findById
        var author = new Author();
        author.setAuthorId(1000L);
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setEmail("john@doe.com");
        author.setBiography("some biography1");

        //Given. Arrange inputs and targets. Define the behavior of Mock object authorRepository.
        given(this.authorRepository.findById(1000L)).willReturn(Optional.of(author));

        //When
        var returnedAuthor = this.authorService.findAuthorById(1000L);

        //Then
        assertThat(returnedAuthor.getAuthorId()).isEqualTo(author.getAuthorId());
        assertThat(returnedAuthor.getFirstName()).isEqualTo(author.getFirstName());
        assertThat(returnedAuthor.getLastName()).isEqualTo(author.getLastName());
        assertThat(returnedAuthor.getEmail()).isEqualTo(author.getEmail());
        assertThat(returnedAuthor.getBiography()).isEqualTo(author.getBiography());
        verify(this.authorRepository, times(1)).findById(1000L);
    }

    @Test
    void findAuthorByIdNotFound() {

        //Given
        given(this.authorRepository.findById(Mockito.any(Long.class))).willReturn(Optional.empty());

        //When
        Throwable thrown = catchThrowable(() -> {
            this.authorService.findAuthorById(1000L);
        });


        //Then
        assertThat(thrown)
                .isInstanceOf(AuthorNotFoundException.class)
                .hasMessage("Could not find author with Id 1000" );
        verify(authorRepository, times(1)).findById(Mockito.any(Long.class));
    }

    @Test
    void testFindAllAuthorSuccess() {
        //Given
        given(this.authorRepository.findAll()).willReturn(authorList);

        //When
        List<Author> allAuthors = authorService.findAllAuthors();

        //Then
        assertThat(allAuthors.size()).isEqualTo(authorList.size());
        verify(authorRepository, times(1)).findAll();
    }

    @Test
    void testAddAuthorSuccess() {
        //Given
        //prepare data to add
        var newAuthor = new Author();
        newAuthor.setFirstName("John");
        newAuthor.setLastName("Doe");
        newAuthor.setEmail("john@doe.com");
        newAuthor.setBiography("some biography1");

        given(this.authorRepository.save(newAuthor)).willReturn(newAuthor);

        //When
        Author savedAuthor = this.authorService.saveAuthor(newAuthor);

        //Then
        assertThat(savedAuthor.getAuthorId()).isEqualTo(newAuthor.getAuthorId());
        assertThat(newAuthor.getFirstName()).isEqualTo(savedAuthor.getFirstName());
        assertThat(newAuthor.getLastName()).isEqualTo(savedAuthor.getLastName());
        assertThat(newAuthor.getEmail()).isEqualTo(savedAuthor.getEmail());
        assertThat(newAuthor.getBiography()).isEqualTo(savedAuthor.getBiography());
        verify(this.authorRepository, times(1)).save(newAuthor);
    }

    @Test
    void testUpdateAuthorSuccess() {
        //Given
        //prepare the data to be updated. It will be saved by calling the save method in repository.
        var oldAuthor = new Author();
        oldAuthor.setAuthorId(1000L);
        oldAuthor.setFirstName("John");
        oldAuthor.setLastName("Doe");
        oldAuthor.setEmail("john@doe.com");
        oldAuthor.setBiography("some biography1");

        //data provided by the front end
        var updateAuthor = new Author();
        updateAuthor.setAuthorId(1000L);
        updateAuthor.setFirstName(" update John first name");
        updateAuthor.setLastName("Doe");
        updateAuthor.setEmail("john@doe.com");
        updateAuthor.setBiography("some biography1 update");

        //first find the existing book and save
        given(this.authorRepository.findById(oldAuthor.getAuthorId())).willReturn(Optional.of(oldAuthor));
        given(this.authorRepository.save(oldAuthor)).willReturn(oldAuthor);

        //When
        var updatedAuthor = this.authorService.updateAuthor(updateAuthor, oldAuthor.getAuthorId());

        //Then
        assertThat(updatedAuthor.getAuthorId()).isEqualTo(updateAuthor.getAuthorId());
        assertThat(updatedAuthor.getFirstName()).isEqualTo(updateAuthor.getFirstName());
        assertThat(updatedAuthor.getLastName()).isEqualTo(updateAuthor.getLastName());
        assertThat(updatedAuthor.getEmail()).isEqualTo(updateAuthor.getEmail());
        assertThat(updatedAuthor.getBiography()).isEqualTo(updateAuthor.getBiography());
        verify(this.authorRepository, times(1)).findById(oldAuthor.getAuthorId());
    }

    @Test
    void testUpdateAuthorNotFound() {
        //Given
        //old data does not exist, so we only have update data
        var update = new Author();
        update.setFirstName("update first John");
        update.setLastName("update last name Doe");
        update.setEmail("john@doe.com");
        update.setBiography("some biography1");

        given(this.authorRepository.findById(update.getAuthorId())).willReturn(Optional.empty());

        //When
        assertThrows(AuthorNotFoundException.class, () -> {
            this.authorService.updateAuthor(update, update.getAuthorId());
        });

        //Then
        verify(authorRepository, times(1)).findById(update.getAuthorId());
    }

    @Test
    void testDeleteAuthorSuccess() {

        //Given
        var author = new Author();
        author.setAuthorId(1000L);
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setEmail("john@doe.com");
        author.setBiography("some biography1");

        //mock behavior of the findById and deleteById method of the Repository because we have find and then delete
        given(this.authorRepository.findById(author.getAuthorId())).willReturn(Optional.of(author));
        doNothing().when(this.authorRepository).deleteById(author.getAuthorId()); //returns void

        //When
        authorService.deleteAuthor(author.getAuthorId());

        //Then
        verify(this.authorRepository, times(1)).deleteById(author.getAuthorId());
    }

    @Test
    void testDeleteAuthorNotFound() {
        //Given
        Long nonExistingAuthorId = 1000L;

        //mock behavior of the findById and there is no need to mock the behaviour of the deleteById
        given(this.authorRepository.findById(nonExistingAuthorId)).willReturn(Optional.empty());

        //When
        assertThrows(AuthorNotFoundException.class, () -> {
            this.authorService.deleteAuthor(nonExistingAuthorId);
        });

        //Then
        verify(authorRepository, times(1)).findById(nonExistingAuthorId);
    }

    @Test
    void testAssignBookSuccess() {
        //Given
        //create some fake data
        //the goal is now to assign the book that originally belong to John to Bone
        UUID bookId = UUID.randomUUID();
        Date date = new Date(1726680002000L);
        var book = new Book();
        book.setISBN(bookId);
        book.setPublisher("publisher 1");
        book.setTitle("title 1");
        book.setGenre("genre 1");
        book.setDescription("description 1");
        book.setPrice(200.0);
        book.setEdition("edition 1");
        book.setPages(10);
        book.setPublicationDate(date);
        book.setLanguage("book1 language");
        book.setQuantity("10");

        var author1 = new Author();
        author1.setAuthorId(1000L);
        author1.setFirstName("John");
        author1.setLastName("Doe");
        author1.setEmail("john@doe.com");
        author1.setBiography("some biography1");

        author1.addBook(book);

        var author2 = new Author();
        author2.setAuthorId(2000L);
        author2.setFirstName("Bone");
        author2.setLastName("Dre");
        author2.setEmail("dre@doe.com");
        author2.setBiography("some biography2");

        //first find if they both exist, then we can do assignment
        //we will only focus on happy part
        given(this.bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(this.authorRepository.findById(author2.getAuthorId())).willReturn(Optional.of(author2));

        //When
        this.authorService.assignBookToAuthor(2000L, bookId);

        //Then
        assertThat(book.getOwner().getAuthorId()).isEqualTo(author2.getAuthorId());
        assertThat(author2.getBooks()).contains(book);
    }

    @Test
    void testAssignBookFailureWithNonExistingAuthorId() {
        //Given
        //create some fake data
        //the goal is now to assign the book that originally belong to John to Bone
        UUID bookId = UUID.randomUUID();
        Date date = new Date(1726680002000L);
        var book = new Book();
        book.setISBN(bookId);
        book.setPublisher("publisher 1");
        book.setTitle("title 1");
        book.setGenre("genre 1");
        book.setDescription("description 1");
        book.setPrice(200.0);
        book.setEdition("edition 1");
        book.setPages(10);
        book.setPublicationDate(date);
        book.setLanguage("book1 language");
        book.setQuantity("10");

        var author1 = new Author();
        author1.setAuthorId(1000L);
        author1.setFirstName("John");
        author1.setLastName("Doe");
        author1.setEmail("john@doe.com");
        author1.setBiography("some biography1");

        author1.addBook(book);

        //no need to define this, since it does not exist
//        var author2 = new Author();
//        author2.setAuthorId(2000L);
//        author2.setFirstName("Bone");
//        author2.setLastName("Dre");
//        author2.setEmail("dre@doe.com");
//        author2.setBiography("some biography2");

        //first find if they both exist, then we can do assignment
        //we will only focus on happy part
        given(this.bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(this.authorRepository.findById(1000L)).willReturn(Optional.empty());

        //When
        Throwable throwable = assertThrows(AuthorNotFoundException.class, () -> {
            this.authorService.assignBookToAuthor(1000L, bookId);
        });

        //Then
        assertThat(throwable)
                .isInstanceOf(AuthorNotFoundException.class)
                .hasMessage("Could not find author with Id 1000");
        assertThat(book.getOwner().getAuthorId()).isEqualTo(author1.getAuthorId()); //also assert that ownership is not changed

    }

    @Test
    void testAssignBookFailureWithNonExistingBookId() {
        //Given
        //create some fake data
        //the goal is now to assign the book that originally belong to John to Bone

        //No need for this since book does not exist
          UUID bookId = UUID.randomUUID();
//        Date date = new Date(1726680002000L);
//        var book = new Book();
//        book.setISBN(bookId);
//        book.setPublisher("publisher 1");
//        book.setTitle("title 1");
//        book.setGenre("genre 1");
//        book.setDescription("description 1");
//        book.setPrice(200.0);
//        book.setEdition("edition 1");
//        book.setPages(10);
//        book.setPublicationDate(date);
//        book.setLanguage("book1 language");
//        book.setQuantity("10");

        //we don't need this as well because once the book does not exist, the flow will get even get to find author if author exist
//        var author1 = new Author();
//        author1.setAuthorId(1000L);
//        author1.setFirstName("John");
//        author1.setLastName("Doe");
//        author1.setEmail("john@doe.com");
//        author1.setBiography("some biography1");
//
//        author1.addBook(book);
//
//        var author2 = new Author();
//        author2.setAuthorId(2000L);
//        author2.setFirstName("Bone");
//        author2.setLastName("Dre");
//        author2.setEmail("dre@doe.com");
//        author2.setBiography("some biography2");

        //first find if they both exist, then we can do assignment
        //we will only focus on happy part
        given(this.bookRepository.findById(bookId)).willReturn(Optional.empty());


        //When
        Throwable throwable = assertThrows(BookNotFoundException.class, () -> {
            this.authorService.assignBookToAuthor(1000L, bookId);
        });


        //Then
        assertThat(throwable)
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Could not find book with isbn " + bookId);
    }

}