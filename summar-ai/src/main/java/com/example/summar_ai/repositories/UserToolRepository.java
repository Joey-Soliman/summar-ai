package com.example.summar_ai.repositories;

import com.example.summar_ai.models.Tool;
import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserToolRepository extends JpaRepository<UserTool, Long> {
    @Query("SELECT ut FROM UserTool ut JOIN FETCH ut.tool WHERE ut.user.id = :userId")
    List<UserTool> findByUserId(@Param("userId") Long userId);  // Get all tools for a user

    Optional<UserTool> findByUserIdAndToolId(Long userId, Long toolId);  // Get UserTool by User and Tool
}
