package com.example.summar_ai.models;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserToolId implements Serializable {

    private Long userId;
    private Long toolId;

    public UserToolId() {}

    public UserToolId(Long userId, Long toolId) {
        this.userId = userId;
        this.toolId = toolId;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }

    // Override equals and hashCode for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserToolId that = (UserToolId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(toolId, that.toolId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, toolId);
    }
}
