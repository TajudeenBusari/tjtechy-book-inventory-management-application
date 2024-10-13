package com.tjtechy.tjtechyinventorymanagementsept2024.author.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto.AuthorDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.service.AuthorService;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.AuthorNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.BookNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)//turns off spring security
/**
 * this will override the active profile in application.yml file.
 * Irrespective of the active profile, the test will only run using the
 * h2-database
 *
 */
@ActiveProfiles(value = "h2-database")
class AuthorControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthorService authorService;

    @Autowired
    ObjectMapper objectMapper;

    List<Author> authorList;

    @Value("${api.endpoint.base-url}")
    String baseUrl;


    @BeforeEach
    void setUp() {
        UUID bookIsbn1 = UUID.fromString("64534f0e-7525-11ef-b864-0242ac120002");
        Date date = new Date(1726680002000L);

        var book1 = new Book();
        book1.setISBN(bookIsbn1);
        book1.setTitle("Book 1");
        book1.setQuantity("1");
        book1.setPublisher("Book1 Publisher");
        book1.setPublicationDate(date);
        book1.setLanguage("English");
        book1.setEdition("first edition");
        book1.setPages(50);
        book1.setPrice(120.0);
        book1.setGenre("Book1 Genre");
        book1.setDescription("Book1 Description");

        var bookIsbn2 = UUID.fromString("9ab93081-738b-4acb-b0d9-9113c59a38f4");
        var book2 = new Book();
        book2.setISBN(bookIsbn2);
        book2.setTitle("Book 2");
        book2.setQuantity("2");
        book2.setPublisher("Book2 Publisher");
        book2.setPublicationDate(date);
        book2.setLanguage("Yoruba");
        book2.setEdition("second edition");
        book2.setPages(50);
        book2.setPrice(120.0);
        book2.setGenre("Book2 Genre");
        book2.setDescription("Book2 Description");

        var bookIsbn3 = UUID.fromString("76d452b3-add8-4980-9ae9-0750ccd524cf");
        var book3 = new Book();
        book3.setISBN(bookIsbn3);
        book3.setTitle("Book 3");
        book3.setQuantity("3");
        book3.setPublisher("Book3 Publisher");
        book3.setPublicationDate(date);
        book3.setLanguage("Finnish");
        book3.setEdition("third edition");
        book3.setPages(50);
        book3.setPrice(120.0);
        book3.setGenre("Book3 Genre");
        book3.setDescription("Book3 Description");

        authorList = new ArrayList<>(); //initialize list with null

        var author1 = new Author();
        author1.setAuthorId(1000L);
        author1.setFirstName("idowu");
        author1.setLastName("noble");
        author1.setEmail("idowu@gmail.com");
        author1.setBiography("author 1 biography 1");

        author1.addBook(book1);
        author1.addBook(book2);

        authorList.add(author1);//add to list

        var author2 = new Author();
        author2.setAuthorId(2000L);
        author2.setFirstName("john");
        author2.setLastName("trip");
        author2.setEmail("john@gmail.com");
        author2.setBiography("author 2 biography 2");

        author2.addBook(book3);

        authorList.add(author2);//add to list

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindAuthorByIdSuccess() throws Exception {
        //Given
        given(this.authorService.findAuthorById(1000L)).willReturn(authorList.get(0));

        //When and //Then
        this.mockMvc.perform(get(this.baseUrl + "/authors/1000").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.authorId").value(1000))
                .andExpect(jsonPath("$.data.firstName").value("idowu"))
                .andExpect(jsonPath("$.data.lastName").value("noble"))
                .andExpect(jsonPath("$.data.email").value("idowu@gmail.com"))
                .andExpect(jsonPath("$.data.biography").value("author 1 biography 1"));
    }

    @Test
    void testFindAuthorByIdNotFound() throws Exception {
        //Given
        Long nonExistingId = 1000L;
        given(this.authorService.findAuthorById(nonExistingId)).willThrow(new AuthorNotFoundException(nonExistingId));

        //When and //Then
        this.mockMvc.perform(get(this.baseUrl + "/authors/1000").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find author with Id " + nonExistingId))
                .andExpect(jsonPath("$.data").isEmpty());


    }

    @Test
    void testFindAllAuthorSuccess() throws Exception {
        //Given
        given(this.authorService.findAllAuthors()).willReturn(authorList);

        //When and Then
        this.mockMvc.perform(get(this.baseUrl + "/authors").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data[0].authorId").value(1000))
                .andExpect(jsonPath("$.data[0].firstName").value("idowu"))
                .andExpect(jsonPath("$.data[0].lastName").value("noble"))
                .andExpect(jsonPath("$.data[0].email").value("idowu@gmail.com"))
                .andExpect(jsonPath("$.data[0].biography").value("author 1 biography 1"))
                .andExpect(jsonPath("$.data[1].authorId").value(2000))
                .andExpect(jsonPath("$.data[1].firstName").value("john"))
                .andExpect(jsonPath("$.data[1].lastName").value("trip"))
                .andExpect(jsonPath("$.data[1].email").value("john@gmail.com"))
                .andExpect(jsonPath("$.data[1].biography").value("author 2 biography 2"));
    }

    @Test
    void testAddAuthorSuccess() throws Exception {
        //Given
        //prepare a Dto to add
        var authorDto = new AuthorDto(
              null,
              "lamurudu",
              "ajagbe",
                "ajagbe@yahho.com",
                "some lamurudu biography",
                0
        );
        String json = this.objectMapper.writeValueAsString(authorDto);

        //prepare data (domain) that the service layer will save
        var authorToSave = new Author();
        authorToSave.setAuthorId(1000L);
        authorToSave.setFirstName("lamurudu");
        authorToSave.setLastName("ajagbe");
        authorToSave.setEmail("ajagbe@yahho.com");
        authorToSave.setBiography("some lamurudu biography");

        given(this.authorService.saveAuthor(Mockito.any(Author.class))).willReturn(authorToSave);

        //When and Then
        this.mockMvc.perform(post(this.baseUrl + "/authors").contentType(MediaType.APPLICATION_JSON)
                .content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.authorId").isNotEmpty())
                .andExpect(jsonPath("$.data.firstName").value("lamurudu"))
                .andExpect(jsonPath("$.data.lastName").value("ajagbe"))
                .andExpect(jsonPath("$.data.email").value("ajagbe@yahho.com"))
                .andExpect(jsonPath("$.data.biography").value("some lamurudu biography"));
    }

    @Test
    void testUpdateAuthorSuccess() throws Exception {
        //Given
        var authorDto = new AuthorDto(
                1000L,
                "taju",
                "lasisi",
                "taju@lasisi.com",
                "some biography", 0
        );
        var json = this.objectMapper.writeValueAsString(authorDto);

        var updatedAuthor = new Author();
        updatedAuthor.setAuthorId(1000L);
        updatedAuthor.setFirstName("taju update");
        updatedAuthor.setLastName("lasisi update");
        updatedAuthor.setEmail("taju@lasisi.com");
        updatedAuthor.setBiography("some biography");

        given(this.authorService.updateAuthor(Mockito.any(Author.class), eq(1000L))).willReturn(updatedAuthor);

        //When and Then
        this.mockMvc.perform(put(this.baseUrl + "/authors/1000").contentType(MediaType.APPLICATION_JSON)
                .content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.authorId").value(1000))
                .andExpect(jsonPath("$.data.firstName").value("taju update"))
                .andExpect(jsonPath("$.data.lastName").value("lasisi update"))
                .andExpect(jsonPath("$.data.email").value("taju@lasisi.com"))
                .andExpect(jsonPath("$.data.biography").value("some biography"));
    }

    @Test
    void testUpdateAuthorNotFound() throws Exception {
        //Given
        Long nonExistingId = 3000L;

        var authorDto = new AuthorDto(
                nonExistingId,
                "update taju",
                "update lasisi",
                "taju@lasisi.com",
                "some biography",
                0
        );
        String json = this.objectMapper.writeValueAsString(authorDto);
        given(this.authorService.updateAuthor(Mockito.any(Author.class), eq(nonExistingId))).willThrow(new AuthorNotFoundException(nonExistingId));

        //When and Then
        this.mockMvc.perform(put(this.baseUrl + "/authors/{id}", nonExistingId).contentType(MediaType.APPLICATION_JSON)
                .content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find author with Id " + nonExistingId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteAuthorSuccess() throws Exception {
        //Given
        Long existingAuthorId = 1000L;
        doNothing().when(this.authorService).deleteAuthor(existingAuthorId);

        //When and //Then
        this.mockMvc.perform(delete(this.baseUrl + "/authors/{id}", existingAuthorId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteAuthorNotFound() throws Exception {
        //Given
        Long nonExistingId = 3000L;
        doThrow(new AuthorNotFoundException(nonExistingId)).when(this.authorService).deleteAuthor(nonExistingId);

        //When and Then
        this.mockMvc.perform(delete(this.baseUrl + "/authors/{id}", nonExistingId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find author with Id " + nonExistingId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAssignBookSuccess() throws Exception {
        //Given
        UUID bookId = UUID.randomUUID();
        doNothing().when(this.authorService).assignBookToAuthor(1000L, bookId);

        //When and //Then
        this.mockMvc.perform(put(this.baseUrl + "/authors/1000/books/" + bookId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Book Assignment Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAssignBookFailureWithNonExistingAuthorId() throws Exception {
        //Given
        UUID bookId = UUID.randomUUID();

        doThrow(new AuthorNotFoundException(1000L))
                .when(this.authorService)
                .assignBookToAuthor(1000L, bookId);


        //When and //Then
        this.mockMvc.perform(put(this.baseUrl + "/authors/1000/books/" + bookId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find author with Id " + 1000L))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAssignBookFailureWithNonExistingBookId() throws Exception {
        //Given
        UUID bookId = UUID.randomUUID();
        doThrow(new BookNotFoundException(bookId))
        .when(this.authorService)
                .assignBookToAuthor(1000L, bookId);

        //When and //Then
        this.mockMvc.perform(put(this.baseUrl + "/authors/1000/books/" + bookId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find book with isbn " + bookId))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}