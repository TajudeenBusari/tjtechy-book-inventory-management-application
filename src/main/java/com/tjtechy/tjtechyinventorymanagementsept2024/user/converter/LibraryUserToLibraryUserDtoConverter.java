package com.tjtechy.tjtechyinventorymanagementsept2024.user.converter;

import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.dto.LibraryUserDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LibraryUserToLibraryUserDtoConverter implements Converter<LibraryUser, LibraryUserDto> {


    @Override
    public LibraryUserDto convert(LibraryUser source) {
        return new LibraryUserDto(
                source.getUserId(),
                source.getUserName(),
                source.isEnabled(),
                source.getRoles()

        );
    }

}
