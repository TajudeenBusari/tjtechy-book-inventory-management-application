package com.tjtechy.tjtechyinventorymanagementsept2024.book.exceptions.modelNotFound;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(UUID uuid) {
        super("Could not find book with isbn " + uuid);
    }

}
