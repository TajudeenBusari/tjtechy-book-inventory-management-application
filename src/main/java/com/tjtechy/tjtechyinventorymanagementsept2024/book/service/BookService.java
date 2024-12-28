package com.tjtechy.tjtechyinventorymanagementsept2024.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.dto.BookDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.ChatClient;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatRequest;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.Message;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.BookNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.repository.BookRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional //ensures a role back for any method in case of any error
public class BookService {

    private final BookRepository bookRepository;

    private final ChatClient chatClient;

    public BookService(BookRepository bookRepository, ChatClient chatClient) {

        this.bookRepository = bookRepository;
        this.chatClient = chatClient;
    }

    //findByIsbnService will be used as a span name
    @Observed(name = "book", contextualName = "findByIsbnService")
    public Book findByIsbn(UUID bookIsbn) {
        return this.bookRepository
                .findById(bookIsbn)
                .orElseThrow(() -> new BookNotFoundException(bookIsbn));
    }

    @Timed("findAllBooksService.time") //metrics to measure time taken
    public List<Book> findAll() {
        //return List.of(); //empty
        return this.bookRepository.findAll();
    }

    public Book save(Book newBook) {
        //return null;
        return this.bookRepository.save(newBook);
    }

    public Book update(Book updatedBook, UUID bookIsbn) {
        return this.bookRepository.findById(bookIsbn).
                map(oldBook -> {
                        oldBook.setTitle(updatedBook.getTitle());
                        oldBook.setDescription(updatedBook.getDescription());
                        oldBook.setGenre(updatedBook.getGenre());
                        oldBook.setPublisher(updatedBook.getPublisher());
                        oldBook.setPrice(updatedBook.getPrice());
                        oldBook.setPages(updatedBook.getPages());
                        oldBook.setEdition(updatedBook.getEdition());
                        oldBook.setPublicationDate(updatedBook.getPublicationDate());
                        oldBook.setLanguage(updatedBook.getLanguage());
                        oldBook.setQuantity(updatedBook.getQuantity());

                        return this.bookRepository.save(oldBook);
        })
                .orElseThrow(()-> new BookNotFoundException(bookIsbn));
    }

    public void delete(UUID bookIsbn) {
        Book book = this.bookRepository.findById(bookIsbn)
                .orElseThrow(() -> new BookNotFoundException(bookIsbn));
        this.bookRepository.deleteById(bookIsbn);

    }

    public String summarize(List<BookDto> bookDtos) throws JsonProcessingException {

      ObjectMapper objectMapper = new ObjectMapper();
      String jsonArray = objectMapper.writeValueAsString(bookDtos);

      //prepare some messages for summarizing.
      List<Message> messages = List.of(
              new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of books, each book's description and the publisher information. Please don't include the statement, The JSON array is as follows:"),
              new Message("user", jsonArray)
      );
      var chatRequest = new ChatRequest("gpt-4", messages);
      var chatResponse = this.chatClient.generate(chatRequest);
      return chatResponse.choices().get(0).message().content();
    }

    public Page<Book> findAll(Pageable pageable) {
        return this.bookRepository.findAll(pageable);
    }

}
