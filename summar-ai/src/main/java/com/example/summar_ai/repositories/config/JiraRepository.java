package com.example.summar_ai.repositories.config;

import com.example.summar_ai.models.User;
import com.example.summar_ai.models.config.Jira;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraRepository extends JpaRepository<Jira, Long> {
    // Find Jira configuration by user
    Jira findByUser(User user);
}
