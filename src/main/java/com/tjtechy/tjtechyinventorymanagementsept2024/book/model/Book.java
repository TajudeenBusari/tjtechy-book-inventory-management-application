package com.tjtechy.tjtechyinventorymanagementsept2024.book.model;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "books")
public class Book implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private UUID ISBN;

    private String title;

    private String publisher;

    private Date publicationDate;

    private String Genre;

    private String edition;

    private String language;

    private int pages;

    private String description;

    private Double price;

    private String Quantity;

    @ManyToOne
    private Author owner; //Many Books can be owned by one Author

    public Book() {
    }

    public UUID getISBN() {
        return ISBN;
    }

    public void setISBN(UUID ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getGenre() {
        return Genre;
    }

    public void setGenre(String genre) {
        Genre = genre;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public Author getOwner() {
        return owner;
    }

    public void setOwner(Author owner) {
        this.owner = owner;
    }

    public Book(UUID ISBN, String title, String publisher, Date publicationDate, String genre, String edition, String language, int pages, String description, Double price, String quantity) {
        this.ISBN = ISBN;
        this.title = title;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        Genre = genre;
        this.edition = edition;
        this.language = language;
        this.pages = pages;
        this.description = description;
        this.price = price;
        Quantity = quantity;
    }
}
