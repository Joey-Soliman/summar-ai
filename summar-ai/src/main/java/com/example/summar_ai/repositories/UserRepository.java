package com.example.summar_ai.repositories;

import com.example.summar_ai.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // Used for authentication

    boolean existsByUsername(String username); // Check if username already exists
}
