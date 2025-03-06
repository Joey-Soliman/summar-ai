package com.example.summar_ai.services.integrations;

import com.example.summar_ai.services.ToolDataService;
import org.springframework.stereotype.Service;

@Service
public class JiraService implements ToolDataService {
    @Override
    public String fetchData() {
        return "Jira Data: [Example issues, tasks, etc.]";
    }
}

