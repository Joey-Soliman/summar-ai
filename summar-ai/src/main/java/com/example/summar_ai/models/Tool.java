package com.example.summar_ai.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tools")
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String toolName; // Jira, Google Calendar, etc.

    private String provider;

    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL)
    private List<UserTool> userTools;

    // Getters and setters
    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Long getId() {
        return id;
    }

    public String getProvider() { return provider; }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
