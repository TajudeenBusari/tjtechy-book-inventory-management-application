package com.tjtechy.tjtechyinventorymanagementsept2024.exceptions;

public class PasswordChangeIllegalArgumentException extends RuntimeException {

    public PasswordChangeIllegalArgumentException(String message) {
        super(message);
    }
}

/**
 * Remember to handle this exception in the ExceptionHandlerAdvice class
 */
