package com.mshzidan.guard.repository;

import com.mshzidan.guard.security.entites.AuthProvider;
import com.mshzidan.guard.security.entites.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AuthProviderRepository extends CrudRepository<AuthProvider, Long> {
    Optional<AuthProvider> findByUserAndProvider(User user, String provider);
}
