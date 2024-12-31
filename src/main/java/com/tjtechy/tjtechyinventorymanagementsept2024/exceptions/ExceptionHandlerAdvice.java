package com.tjtechy.tjtechyinventorymanagementsept2024.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.AuthorNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.BookNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.LibraryUserNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.Result;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(LibraryUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleLibraryUserNotFoundException(LibraryUserNotFoundException libraryUserNotFoundException) {
        return new Result(false, StatusCode.NOT_FOUND, libraryUserNotFoundException.getMessage());
    }

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
       return new Result(false, StatusCode.BAD_REQUEST, "Provided arguments are invalid, see data for details.", map);
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)//normally, it should be unauthenticated but this is not available
    Result handleAuthenticationException(Exception ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "username or password is incorrect.", ex.getMessage());
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleInsufficientAuthenticationException(InsufficientAuthenticationException ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "Login credentials are missing", ex.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)//normally, it should be unauthenticated but this is not available
    Result handleAccountStatusException(AccountStatusException ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "user account is abnormal.", ex.getMessage());
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)//normally, it should be unauthenticated but this is not available
    Result handleInvalidBearerTokenException(InvalidBearerTokenException ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "The access token is expired, revoked, malformed, or invalid for other reasons.", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)//normally, it should be unauthenticated but this is not available
    Result handleAccessDeniedException(AccessDeniedException ex) {
        return new Result(false, StatusCode.FORBIDDEN, "No permission", ex.getMessage());
    }

    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    ResponseEntity<Result> handleRestClientException(HttpStatusCodeException ex) throws JsonProcessingException {
        var exMessage = ex.getMessage();

        //Replace <EOL> with actual newline
        exMessage = exMessage.replace("<EOL>", "\n");

        //Extract the json part from the string
        var jsonPart = exMessage.substring(exMessage.indexOf("{"), exMessage.lastIndexOf("}") + 1);

        //create an objectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        //parse the JSON string to a JsonNode
        var rootNode = objectMapper.readTree(jsonPart);

        //extract the message.
        var formattedExceptionMessage = rootNode.path("error").path("message").asText();

        return new ResponseEntity<>(
                new Result(false,
                        ex.getStatusCode().value(),
                        "A rest client error occurs, see data for details.",
                        formattedExceptionMessage),
                ex.getStatusCode());
    }

    @ExceptionHandler(PasswordChangeIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handlePasswordChangeIllegalArgumentException(PasswordChangeIllegalArgumentException ex) {
        return new Result(false, StatusCode.BAD_REQUEST, "Password change failed", ex.getMessage());
    }


    /**
     * fall back handles any unhandled exception
     * @param ex
     * @return
     * /** and enter to get this
     */
    //for any unhandled exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//normally, it should be unauthenticated but this is not available
    Result handleOtherException(Exception ex) {
        return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, "A server internal error occurs", ex.getMessage());
    }





    /*
    * StatusCode.NOT_FOUND is in the response header
    * StatusCode.NOT_FOUND the one will define ourselves, is in the response body
    *
    * */
}
