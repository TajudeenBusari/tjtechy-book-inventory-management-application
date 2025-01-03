package com.tjtechy.tjtechyinventorymanagementsept2024.security;

import com.tjtechy.tjtechyinventorymanagementsept2024.client.rediscache.RedisCacheClient;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.converter.LibraryUserToLibraryUserDtoConverter;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.MyUserPrincipal;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.dto.LibraryUserDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;

    private final LibraryUserToLibraryUserDtoConverter libraryUserToLibraryUserDtoConverter;

    private final RedisCacheClient redisCacheClient;

    public AuthService(JwtProvider jwtProvider, LibraryUserToLibraryUserDtoConverter libraryUserToLibraryUserDtoConverter, RedisCacheClient redisCacheClient) {
        this.jwtProvider = jwtProvider;
        this.libraryUserToLibraryUserDtoConverter = libraryUserToLibraryUserDtoConverter;
      this.redisCacheClient = redisCacheClient;
    }

    public Map<String, Object> createLoginInfo(Authentication authentication) {

        /*
        * since this method wil require two entries user info and token,
        * we will have the two here
        * */

        //create user info.
        MyUserPrincipal principal = (MyUserPrincipal)authentication.getPrincipal();
        LibraryUser libraryUser = principal.getLibraryUser();

        //convert to Dto, libraryUser has password
        LibraryUserDto libraryUserDto = this.libraryUserToLibraryUserDtoConverter.convert(libraryUser);

        //create token.
        String token = this.jwtProvider.createToken(authentication);
        //String token = ""; just for testing

        //save a copy of the token in redis before returning to the client. Key is "whitelist: {userId}", value is token
        //if a user logs in multiple times, only the last token will be saved because, the key is unique.
        this.redisCacheClient.set("whitelist:" + libraryUser.getUserId(), token, 2, TimeUnit.HOURS);

        Map<String, Object> loginResultMap = new HashMap<>();
        loginResultMap.put("userInfo", libraryUserDto);
        loginResultMap.put("token", token);

        return loginResultMap;

    }
}
