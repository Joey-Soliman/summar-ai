package com.example.summar_ai.controllers;

import com.example.summar_ai.models.Tool;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.services.ToolService;
import com.example.summar_ai.services.AuthService;
import com.example.summar_ai.models.User;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ToolController {

    private final ToolService toolService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(ToolController.class);

    @Autowired
    public ToolController(ToolService toolService, AuthService authService) {
        this.toolService = toolService;
        this.authService = authService;
    }

    // Display user tool selection page
    @GetMapping("/user-tools")
    public String showToolsPage(Model model) {
        // Get the authenticated user via AuthService
        User user = authService.getAuthenticatedUser(); // This method encapsulates the logic to get the authenticated user

        // Get list of all tools and the user's current tools
        List<Tool> allTools = toolService.getAllTools();
        List<UserTool> userTools = toolService.getUserTools(user.getId());
        // Filter out userTools
        List<Tool> userOwnedTools = userTools.stream()
                .map(UserTool::getTool) // Extract Tool from UserTool
                .collect(Collectors.toList());

        List<Tool> availableTools = allTools.stream()
                .filter(tool -> !userOwnedTools.contains(tool)) // Now the check is valid
                .collect(Collectors.toList());

        model.addAttribute("tools", availableTools); // Get all available tools
        model.addAttribute("user", user); // Pass the user object to the view for display
        model.addAttribute("userTools", userTools);
        model.addAttribute("userOwnedTools", userOwnedTools);

        // Initialize selectedTool to null initially (no tool selected yet)
        model.addAttribute("selectedTool", null);

        return "user-tools"; // Render the user-tools.html page
    }

    // Handle tool selection by the user
    @PostMapping("/user-tools")
    public String selectToolForUser(@RequestParam("toolName") String toolName,
                                    @RequestParam("userId") Long userId,
                                    Model model,
                                    HttpSession session) {
        // Get the tool from the service by toolName
        Tool selectedTool = toolService.findByToolName(toolName);

        // Add selected tool to the user
        toolService.addToolToUser(userId, selectedTool.getToolName());

        // Set the selectedTool so we can use it in the view for OAuth flow
        model.addAttribute("selectedTool", selectedTool);

        // store original authentication and tool
        session.setAttribute("ORIGINAL_AUTH", SecurityContextHolder.getContext().getAuthentication());
        session.setAttribute("toolName", toolName);

        // Redirect to initiate OAuth authorization for the selected tool
        return "redirect:/oauth2/authorization/" + selectedTool.getProvider();
    }

    // Handle tool removal by the user
    @PostMapping("/remove-tool")
    public String removeToolFromuser(@RequestParam("userId") Long userId, @RequestParam("toolName") String toolName) {
        toolService.removeToolFromUser(userId, toolName);
        return "redirect:/user-tools";
    }
}
