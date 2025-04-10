package com.example.summar_ai.controllers;

import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.repositories.UserToolRepository;
import com.example.summar_ai.services.AuthService;
import com.example.summar_ai.services.ReportService;
import com.example.summar_ai.services.integrations.GPTService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;
    private final UserToolRepository userToolRepository;
    private final GPTService gptService;

    @Autowired
    public ReportController(ReportService reportService, AuthService authService,
                            UserToolRepository userToolRepository, GPTService gptService) {
        this.reportService = reportService;
        this.authService = authService;
        this.userToolRepository = userToolRepository;
        this.gptService = gptService;
    }

    @GetMapping("/generate")
    public String generateReport(Model model, HttpSession session) {
        // Retrieve date range and time zone and convert to rfc3339 format
        LocalDate startDate = (LocalDate) session.getAttribute("startDate");
        LocalDate endDate = (LocalDate) session.getAttribute("endDate");
        ZoneId timeZone = ZoneId.of((String) session.getAttribute("timeZone"));


        // Get user and user's activated tools
        User user = authService.getAuthenticatedUser();
        List<UserTool> activeTools = userToolRepository.findByUserIdAndActivatedTrue(user.getId());

        // Collect data asynchronously from all tools
        CompletableFuture<String> reportFuture = reportService.collectDataFromTools(activeTools, startDate, endDate, timeZone);

        // Once the data is collected, set it to the model
        String reportData = reportFuture.join();

        // Send the data to chat GPT

        // String report = gptService.summarizeReport(reportData);
        // model.addAttribute("report", report);
        model.addAttribute("report", reportData);
        // System.out.println("reportData: " + reportData);
        // System.out.println("report: " + report);

        // Return the view (report)
        return "report";
    }
}

