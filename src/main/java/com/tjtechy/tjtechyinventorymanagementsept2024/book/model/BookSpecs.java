package com.tjtechy.tjtechyinventorymanagementsept2024.book.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class BookSpecs {

  public static Specification<Book> hasISBN(UUID providedISBN){
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("ISBN"), providedISBN);
  }

  public static Specification<Book> containsTitle(String providedTitle){
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + providedTitle.toLowerCase() + "%");
  }

  public static Specification<Book> containsDescription(String providedDescription){
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + providedDescription.toLowerCase() + "%");

  }

  public static Specification<Book> hasOwnerFirstName(String providedOwnerFirstName){
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(criteriaBuilder.lower(root.get("owner").get("firstName")), providedOwnerFirstName.toLowerCase());
  }

  public static Specification<Book> hasOwnerLastName(String providedOwnerLastName) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(criteriaBuilder.lower(root.get("owner").get("lastName")), providedOwnerLastName.toLowerCase());
  }
}




/**
 * The BookSpecs class is used to create a Specification object that can be used to query the database
 * The root represents the entity that is being queried, book in this case.
 * query is used for ordering and grouping the results of the query or specifying want a distinct result.
 * criteriaBuilder is used to construct the search criteria.
 * toPredicate method translate to this sql query: SELECT * FROM books WHERE ISBN = providedISBN
 * Modify the book repository interface to include the methods
 */