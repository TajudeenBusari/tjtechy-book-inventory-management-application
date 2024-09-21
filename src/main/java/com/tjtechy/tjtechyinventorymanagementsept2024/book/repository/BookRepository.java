package com.tjtechy.tjtechyinventorymanagementsept2024.book.repository;

import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository  extends JpaRepository<Book, UUID> {

}

/*
* spring data jpa helps us insert book and authors to database.
* it helps maps java object to relational data model using
* Object/Relational Mapping (ORM)
* */