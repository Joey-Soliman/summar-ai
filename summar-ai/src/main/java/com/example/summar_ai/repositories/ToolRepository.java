package com.example.summar_ai.repositories;

import com.example.summar_ai.models.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Long> {
    // Find tool by name
    Optional<Tool> findByToolName(String toolName);

    // Find toolId by name
    @Query("SELECT t.id FROM Tool t WHERE t.toolName = :toolName")
    Optional<Long> findIdByToolName(@Param("toolName") String toolName);

}
