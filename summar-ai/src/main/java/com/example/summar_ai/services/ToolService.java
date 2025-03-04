package com.example.summar_ai.services;

import com.example.summar_ai.models.Tool;
import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.repositories.ToolRepository;
import com.example.summar_ai.repositories.UserRepository;
import com.example.summar_ai.repositories.UserToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ToolService {

    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final UserToolRepository userToolRepository;

    @Autowired
    public ToolService(ToolRepository toolRepository,
                       UserRepository userRepository,
                       UserToolRepository userToolRepository) {
        this.toolRepository = toolRepository;
        this.userRepository = userRepository;
        this.userToolRepository = userToolRepository;
    }

    // Add tool to user
    public void addToolToUser(Long userId, String toolName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Tool tool = toolRepository.findByToolName(toolName);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        if (userToolRepository.findByUserIdAndToolId(userId, tool.getId()).isPresent()) {
            throw new RuntimeException("User already has this tool");
        }
        UserTool userTool = new UserTool(user, tool, true);
        userToolRepository.save(userTool);
    }

    // Remove tool from user
    public void removeToolFromUser(Long userId, String toolName) {
        Tool tool = toolRepository.findByToolName(toolName);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }

        UserTool userTool = userToolRepository.findByUserIdAndToolId(userId, tool.getId())
                .orElseThrow(() -> new RuntimeException("UserTool association not found"));

        userToolRepository.delete(userTool);
    }



    // Get users tools
    public List<UserTool> getUserTools(Long userId) {
        return userToolRepository.findByUserId(userId);
    }

    // Get all tools
    public List<Tool> getAllTools() {
        return toolRepository.findAll(); // Fetches all tools from the repository
    }

    // Update tool activation
    public void updateToolActivation(Long userId, Long toolId, boolean isActivated) {
        // Check if the user has the tool, then update the activation status
        UserTool userTool = userToolRepository.findByUserIdAndToolId(userId, toolId)
                .orElseThrow(() -> new RuntimeException("UserTool association not found"));
        if (userTool != null) {
            userTool.setActivated(isActivated);
            userToolRepository.save(userTool);
        }
    }
}
