package com.tjtechy.tjtechyinventorymanagementsept2024.book.service;

import com.tjtechy.tjtechyinventorymanagementsept2024.book.exceptions.modelNotFound.BookNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional //ensures a role back for any method in case of any error
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book findByIsbn(UUID bookIsbn) {
        return this.bookRepository
                .findById(bookIsbn)
                .orElseThrow(() -> new BookNotFoundException(bookIsbn));
    }

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

}
