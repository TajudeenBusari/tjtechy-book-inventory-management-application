package com.tjtechy.tjtechyinventorymanagementsept2024.author.controller;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.converter.AuthorDtoToAuthorConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.converter.AuthorToAuthorDtoConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.dto.AuthorDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.service.AuthorService;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.Result;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.endpoint.base-url}/authors")
public class AuthorController {

    private final AuthorService authorService;
    private final AuthorToAuthorDtoConverter authorToAuthorDtoConverter;
    private final AuthorDtoToAuthorConverter authorDtoToAuthorConverter;

    public AuthorController(AuthorService authorService, AuthorToAuthorDtoConverter authorToAuthorDtoConverter, AuthorDtoToAuthorConverter authorDtoToAuthorConverter) {
        this.authorService = authorService;
        this.authorToAuthorDtoConverter = authorToAuthorDtoConverter;
        this.authorDtoToAuthorConverter = authorDtoToAuthorConverter;
    }

    @GetMapping("/{authorId}")
    public Result findAuthorById(@PathVariable Long authorId) {

        var foundAuthor = this.authorService.findAuthorById(authorId);
        var foundAuthorDto = this.authorToAuthorDtoConverter.convert(foundAuthor);

        return new Result(true, StatusCode.SUCCESS, "Find One Success", foundAuthorDto);

    }

    @GetMapping
    public Result findAllAuthors() {

        List<Author> foundAuthors = this.authorService.findAllAuthors();

        //convert to Dto
        List<AuthorDto> authorsDto = foundAuthors
                .stream()
                .map(foundAuthor -> this.authorToAuthorDtoConverter
                        .convert(foundAuthor))
                .collect(Collectors.toList());

        return new Result(true, StatusCode.SUCCESS, "Find All Success", authorsDto);
    }

    @PostMapping
    public Result addAuthor(@RequestBody AuthorDto authorDto) {

        //convert to domain
        var newAuthor = this.authorDtoToAuthorConverter.convert(authorDto);

        Author savedAuthor = this.authorService.saveAuthor(newAuthor);

        //Convert back to Dto
        var savedAuthorDto = this.authorToAuthorDtoConverter.convert(savedAuthor);

        return new Result(true, StatusCode.SUCCESS, "Add Success", savedAuthorDto);
    }

    @PutMapping("/{authorId}")
    public Result updateAuthor(@Valid @RequestBody AuthorDto authorDto, @PathVariable Long authorId) {

        //convert the Dto to domain
        var update = this.authorDtoToAuthorConverter.convert(authorDto);
        var updatedAuthor = this.authorService.updateAuthor(update, authorId);

        //convert back to Dto
        var updatedAuthorDto = this.authorToAuthorDtoConverter.convert(updatedAuthor);

        return new Result(true, StatusCode.SUCCESS, "Update Success", updatedAuthorDto);

    }

    @DeleteMapping("/{authorId}")
    public Result deleteAuthor(@PathVariable Long authorId) {
        this.authorService.deleteAuthor(authorId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }

}
