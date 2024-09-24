package com.tjtechy.tjtechyinventorymanagementsept2024.author.service;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.repository.AuthorRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.AuthorNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.BookNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.repository.BookRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public Author findAuthorById(Long id) {
        return  this.authorRepository
                .findById(id)
                .orElseThrow(()-> new AuthorNotFoundException(id));

    }

    public List<Author> findAllAuthors() {

        return this.authorRepository.findAll();
    }

    public Author saveAuthor(Author newAuthor) {

        return this.authorRepository.save(newAuthor);
    }

    public Author updateAuthor(Author author, Long id) {

        return this.authorRepository.findById(id)
                .map(oldAuthor -> {
                        oldAuthor.setFirstName(author.getFirstName());
                        oldAuthor.setLastName(author.getLastName());
                        oldAuthor.setEmail(author.getEmail());
                        oldAuthor.setBiography(author.getBiography());

                        return this.authorRepository.save(oldAuthor);
        })
                .orElseThrow(()-> new AuthorNotFoundException(id));
    }

    public void deleteAuthor(Long id) {
        Author authorTobeDeleted = this.authorRepository.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
        //before deleting, you must unassign all books owned by the author
        authorTobeDeleted.removeAllBooks();

        this.authorRepository.deleteById(id);
    }

    public void assignBookToAuthor(UUID bookId, Long authorId) {
        //book to be assigned. First check if it exists in the DB
        var bookToBeAssigned = this.bookRepository
                .findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        //Find the author if it exists in the DB
        var author = authorRepository
                .findById(authorId).orElseThrow(() -> new AuthorNotFoundException(authorId));

        //Book assignment
        if (bookToBeAssigned.getOwner() != null) {
            bookToBeAssigned.getOwner().removeBook(bookToBeAssigned);
        }
        author.addBook(bookToBeAssigned);

    }

}

