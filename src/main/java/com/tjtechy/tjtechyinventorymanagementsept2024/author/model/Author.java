package com.tjtechy.tjtechyinventorymanagementsept2024.author.model;

import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "author")
public class Author implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long authorId;

    private String firstName;

    private String lastName;

    private String email;

    private String biography;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "owner" ) //owner points to a foreign key

    //Books already initialized to an empty array
    private List<Book> books = new ArrayList<>();//One Author has many books


    public Author() {
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Author(Long authorId, String firstName, String lastName, String email, String biography) {
        this.authorId = authorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.biography = biography;
    }

    public void addBook(Book book) {
        // the bidirectional relationship between book and author  is established
        book.setOwner(this);
        this.books.add(book);
    }

    public Integer getNumberOfBooks() {
        return books.size();
    }

    public void removeBook(Book bookTobeAssigned) {
        //remove book owner-->you remove owner(author), it removes their book
        bookTobeAssigned.setOwner(null);
        this.books.remove(bookTobeAssigned);
    }

    public void removeAllBooks() {
        this.books.stream().forEach(book -> book.setOwner(null)); //stream through all their books
        this.books = new ArrayList<>(); //empty the list
    }
}
/*
* if we save one author to the db, all books associated with the author will be saved as well
* It is called cascading persist and cascading merge
* */