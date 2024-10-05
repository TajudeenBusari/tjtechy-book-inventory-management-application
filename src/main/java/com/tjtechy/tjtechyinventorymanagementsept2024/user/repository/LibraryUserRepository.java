package com.tjtechy.tjtechyinventorymanagementsept2024.user.repository;

import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryUserRepository extends JpaRepository<LibraryUser, Integer> {

    //define a custom query to find user by username since the default CRUD ops in the repo does not support this
    //spring data jpa will provide the implementation since it has findById and we have followed the same naming convention
    //NOTE: in my model class (LibraryUser), it is userName and not username, so take note and name the
    // find by username in this way findByUserName and not findByUsername

    Optional<LibraryUser> findByUserName(String username);


    //if you need to find by enabled, just follow the naming convention but for now, we don't need it.
    //List<LibraryUser> findByEnabled(boolean enabled);

    //if I need to find by two params
    //corresponds to this SQL SELECT* FROM LIBRARY_USER WHERE username=? AND password=?
    //it is called derived query method
    //Optional<LibraryUser> findByUsernameAndPassword(String username, String password);

}
