package com.tjtechy.tjtechyinventorymanagementsept2024.user.service;

import com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound.LibraryUserNotFoundException;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.repository.LibraryUserRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class LibraryUserService {

    private final LibraryUserRepository libraryUserRepository;

    public LibraryUserService(LibraryUserRepository libraryUserRepository) {

        this.libraryUserRepository = libraryUserRepository;
    }

    public LibraryUser save(LibraryUser newLibraryUser) {

        //We NEED to encode plain text password before saving to the DB! TODO
        
        return libraryUserRepository.save(newLibraryUser);
    }

    public LibraryUser findById(Integer userId) {

        return this.libraryUserRepository.findById(userId)
                .orElseThrow(() -> new LibraryUserNotFoundException(userId));
    }

    public List<LibraryUser> findAll() {

        return this.libraryUserRepository.findAll();
    }

    public LibraryUser update(LibraryUser updateLibraryUser, Integer userId) {

        //find first
        LibraryUser foundlibraryUser = this.libraryUserRepository
                .findById(userId)
                .orElseThrow(() -> new LibraryUserNotFoundException(userId));

        //update we are not updating password here
        foundlibraryUser.setUserName(updateLibraryUser.getUserName());

        foundlibraryUser.setRoles(updateLibraryUser.getRoles());
        foundlibraryUser.setEnabled(updateLibraryUser.isEnabled());

        return this.libraryUserRepository.save(foundlibraryUser);
    }

    public void delete(Integer userId) {
        this.libraryUserRepository.findById(userId)
                .orElseThrow(() -> new LibraryUserNotFoundException(userId));
        this.libraryUserRepository.deleteById(userId);

    }

}
