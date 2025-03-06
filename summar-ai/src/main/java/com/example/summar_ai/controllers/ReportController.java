package com.example.summar_ai.controllers;

import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.repositories.UserToolRepository;
import com.example.summar_ai.services.AuthService;
import com.example.summar_ai.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
    public String generateReport(Model model) {
        User user = authService.getAuthenticatedUser();
        List<UserTool> activeTools = userToolRepository.findByUserIdAndActivatedTrue(user.getId());

        String reportData = reportService.collectDataFromTools(activeTools);
        model.addAttribute("reportData", reportData);
        return "report";
    }
}

