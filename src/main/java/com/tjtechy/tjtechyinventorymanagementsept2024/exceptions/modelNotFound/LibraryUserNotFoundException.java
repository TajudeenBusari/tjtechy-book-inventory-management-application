package com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound;

public class LibraryUserNotFoundException extends RuntimeException{
    public LibraryUserNotFoundException(Integer id) {
        super("Could not find library user with Id " + id);
    }
}
