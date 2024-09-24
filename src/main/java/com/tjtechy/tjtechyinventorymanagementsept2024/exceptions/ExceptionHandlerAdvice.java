package com.tjtechy.tjtechyinventorymanagementsept2024.exceptions;

import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.AuthorNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.BookNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.Result;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleBookNotFoundException(BookNotFoundException bookNotFoundException) {
        return new Result(false, StatusCode.NOT_FOUND, bookNotFoundException.getMessage());

    }

    @ExceptionHandler(AuthorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleAuthorNotFoundException(AuthorNotFoundException authorNotFoundException) {
        return new Result(false, StatusCode.NOT_FOUND, authorNotFoundException.getMessage());
    }

    //in case of any invalid data passed in the request body

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handleValidationException(MethodArgumentNotValidException validationException) {
        List<ObjectError> fieldErrors = validationException.getAllErrors();
        Map<String, String> map = new HashMap<>(fieldErrors.size());
       fieldErrors.forEach(error -> {
           String key = ((FieldError) error).getField();
           String value = ((FieldError) error).getDefaultMessage();
           map.put(key, value);
       });
       return new Result(false, StatusCode.BAD_REQUEST, "Provide arguments are invalid, see data for details.", map);
    }



    /*
    * StatusCode.NOT_FOUND is in the response header
    * StatusCode.NOT_FOUND the one will define ourselves, is in the response body
    *
    * */
}
