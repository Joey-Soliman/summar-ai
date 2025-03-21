package com.example.summar_ai.controllers;

import com.example.summar_ai.models.Tool;
import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.services.AuthService;
import com.example.summar_ai.services.ToolService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ToolService toolService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    public DashboardController(ToolService toolService, AuthService authService) {
        this.toolService = toolService;
        this.authService = authService;
    }
    // Maps to /dashboard route after successful login
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // logger.info("In showDashboard");
        // Get default dates
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(7);
        model.addAttribute("startDate", lastWeek);
        model.addAttribute("endDate", today);

        // Get the authenticated user via AuthService
        User user = authService.getAuthenticatedUser();

        // Get list of all user's current tools
        List<UserTool> userTools = toolService.getUserTools(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("userTools", userTools);

        return "dashboard"; // This will map to /src/main/resources/templates/dashboard.html
    }

    @PostMapping("/dashboard/updateToolActivation")
    public String updateToolActivation(@RequestParam Map<String, String> activatedTools,
                                       @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                       @RequestParam("timeZone") String timeZone,
                                       HttpSession session) {
        // logger.info("In updateToolActivation");
        // Add date range to session
        session.setAttribute("startDate", startDate);
        session.setAttribute("endDate", endDate);
        session.setAttribute("timeZone", timeZone);

        // Iterate through the map to process activated tools
        for (Map.Entry<String, String> entry : activatedTools.entrySet()) {
            // Extract the tool ID from the name (which is in the form "tool_123")
            if (entry.getKey().startsWith("tool_")) {  // This ensures we're processing tool IDs
                Long toolId = Long.parseLong(entry.getKey().substring(5));  // Remove "tool_" prefix and parse the number
                boolean isActivated = Boolean.parseBoolean(entry.getValue());  // Get the activation status (true/false)

                User user = authService.getAuthenticatedUser();  // Get the authenticated user
                toolService.updateToolActivation(user.getId(), toolId, isActivated);  // Update tool activation

                logger.info("Updated user {}: toolId {} is activated {}", user.getId(), toolId, isActivated);
            }
        }

        return "redirect:/report/generate";  // Redirect after updating
    }

}
