package com.example.summar_ai.models;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_tools")
public class UserTool {

    @EmbeddedId
    private UserToolId id; // Composite Key

    @ManyToOne
    @MapsId("userId") // Maps this to user_id in UserToolId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("toolId") // Maps this to tool_id in UserToolId
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    private boolean activated;

    // OAuth token fields
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt; // Store expiration time

    // Constructors
    public UserTool() {}

    public UserTool(User user, Tool tool, boolean activated) {
        this.id = new UserToolId(user.getId(), tool.getId());
        this.user = user;
        this.tool = tool;
        this.activated = activated;
    }

    // Getters and Setters
    public UserToolId getId() { return id; }
    public void setId(UserToolId id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Tool getTool() { return tool; }
    public void setTool(Tool tool) { this.tool = tool; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
