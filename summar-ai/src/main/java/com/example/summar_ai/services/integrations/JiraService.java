package com.example.summar_ai.services.integrations;

import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.services.ToolDataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class JiraService implements ToolDataService {
    @Async
    @Override
    public String fetchData(UserTool userTool, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        System.out.println("JiraService fetching data...");
        String response = "Jira Data: [Example issues, tasks, etc.]";
        return response;
    }
}

