package com.tjtechy.tjtechyinventorymanagementsept2024.user.converter;

import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.dto.LibraryUserDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LibraryUserDtoToLibraryUserConverter implements Converter<LibraryUserDto, LibraryUser> {
    /**
     * @param source
     * @return
     */

    @Override
    public LibraryUser convert(LibraryUserDto source) {

        var libraryUser = new LibraryUser();

        libraryUser.setUserId(source.userId());
        libraryUser.setUserName(source.userName());
        libraryUser.setRoles(source.roles());

        libraryUser.setEnabled(source.enabled());

        return libraryUser;
    }
}
