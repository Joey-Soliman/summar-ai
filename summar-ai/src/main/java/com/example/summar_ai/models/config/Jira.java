package com.example.summar_ai.models.config;

import com.example.summar_ai.models.User;
import jakarta.persistence.*;

@Entity
@Table(name = "jira_configurations")
public class Jira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String jiraBoard;  // Store board information or settings

    // Getters and setters
    public String getJiraBoard() {
        return jiraBoard;
    }

    public void setJiraBoard(String jiraBoard) {
        this.jiraBoard = jiraBoard;
    }
}
