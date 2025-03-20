package com.example.summar_ai.controllers;

import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.repositories.UserToolRepository;
import com.example.summar_ai.services.AuthService;
import com.example.summar_ai.services.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;
    private final UserToolRepository userToolRepository;

    @Autowired
    public ReportController(ReportService reportService, AuthService authService, UserToolRepository userToolRepository) {
        this.reportService = reportService;
        this.authService = authService;
        this.userToolRepository = userToolRepository;
    }

    @GetMapping("/generate")
    public String generateReport(Model model, HttpSession session) {
        // Retrieve date range
        LocalDate startDate = (LocalDate) session.getAttribute("startDate");
        LocalDate endDate = (LocalDate) session.getAttribute("endDate");
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);

        User user = authService.getAuthenticatedUser();
        List<UserTool> activeTools = userToolRepository.findByUserIdAndActivatedTrue(user.getId());

        // Collect data asynchronously from all tools
        CompletableFuture<String> reportFuture = reportService.collectDataFromTools(activeTools);

        // Once the data is collected, set it to the model
        reportFuture.thenAccept(reportData -> {
            model.addAttribute("reportData", reportData);
        });

        // Return the view (report)
        return "report";
    }
}

