package com.tjtechy.tjtechyinventorymanagementsept2024.author.repository;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
