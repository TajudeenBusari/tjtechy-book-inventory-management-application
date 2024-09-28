package com.tjtechy.tjtechyinventorymanagementsept2024.user.controller;


import com.tjtechy.tjtechyinventorymanagementsept2024.system.Result;
import com.tjtechy.tjtechyinventorymanagementsept2024.system.StatusCode;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.converter.LibraryUserDtoToLibraryUserConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.converter.LibraryUserToLibraryUserDtoConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.dto.LibraryUserDto;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.service.LibraryUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class LibraryUserController {

    private final LibraryUserService libraryUserService;

    private final LibraryUserToLibraryUserDtoConverter libraryUserToLibraryUserDtoConverter;

    private final LibraryUserDtoToLibraryUserConverter libraryUserDtoToLibraryUserConverter;

    public LibraryUserController(LibraryUserService libraryUserService, LibraryUserToLibraryUserDtoConverter libraryUserToLibraryUserDtoConverter, LibraryUserDtoToLibraryUserConverter libraryUserDtoToLibraryUserConverter) {

        this.libraryUserService = libraryUserService;
        this.libraryUserToLibraryUserDtoConverter = libraryUserToLibraryUserDtoConverter;
        this.libraryUserDtoToLibraryUserConverter = libraryUserDtoToLibraryUserConverter;
    }

    //since the user needs to pass the password a part of request when adding user,
    // so we use the LibraryUser and not the Dto
    @PostMapping
    public Result addUser(@Valid @RequestBody LibraryUser libraryUser) {

        var savedUser = this.libraryUserService.save(libraryUser);

        //convert back to Dto
        var savedUserDto = libraryUserToLibraryUserDtoConverter.convert(savedUser);

        return new Result(true, StatusCode.SUCCESS, "Add Success", savedUserDto);

    }

    @GetMapping("/{userId}")
        public Result findUserById(@PathVariable Integer userId) {

        LibraryUser foundUser = this.libraryUserService.findById(userId);
        //convert to dto
        var libraryUserDto = this.libraryUserToLibraryUserDtoConverter.convert(foundUser);
        return new Result(true, StatusCode.SUCCESS, "Find One Success", libraryUserDto);
    }

    @GetMapping
    public Result findAllUsers() {

        List<LibraryUser> foundUsers = this.libraryUserService.findAll();
        //convert to Dto
        List<LibraryUserDto> foundUserDtos = foundUsers.stream()
                .map(this.libraryUserToLibraryUserDtoConverter::convert)
                .collect(Collectors.toList());

        return new Result(true, StatusCode.SUCCESS, "Find All Success", foundUserDtos);
    }

    // We are not using this to update password, need another changePassword method in this class.
    @PutMapping("/{userId}")
    public Result updateUserById(@Valid @RequestBody LibraryUserDto libraryUser, @PathVariable Integer userId) {
        //convert Dto to libraryUser first
        var libUser = this.libraryUserDtoToLibraryUserConverter.convert(libraryUser);
        var updatedLibraryUser = this.libraryUserService.update(libUser, userId);

        //convert back to Dto
        var libraryUserDto = this.libraryUserToLibraryUserDtoConverter.convert(updatedLibraryUser);

        return new Result(true, StatusCode.SUCCESS, "Update Success", libraryUserDto);
    }

    @DeleteMapping("{userId}")
    public Result deleteUserById(@PathVariable Integer userId) {

        this.libraryUserService.delete(userId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");

    }
}
