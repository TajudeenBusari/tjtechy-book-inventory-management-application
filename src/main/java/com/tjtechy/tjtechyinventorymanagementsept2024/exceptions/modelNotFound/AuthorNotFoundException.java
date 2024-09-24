package com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound;

public class AuthorNotFoundException extends RuntimeException{
    public AuthorNotFoundException(Long id) {

        super("Could not find author with Id " + id);
    }
}
//AuthorNotFoundException