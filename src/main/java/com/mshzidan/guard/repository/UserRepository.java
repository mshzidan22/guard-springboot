package com.mshzidan.guard.repository;

import com.mshzidan.guard.security.entites.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByPhone(String phone);

}
