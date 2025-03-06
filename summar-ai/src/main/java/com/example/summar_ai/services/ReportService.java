package com.example.summar_ai.services;

import com.example.summar_ai.models.UserTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final Map<String, ToolDataService> toolServiceMap;

    @Autowired
    public ReportService(List<ToolDataService> toolServices) {
        this.toolServiceMap = new HashMap<>();

        // Automatically register services based on their class names
        for (ToolDataService service : toolServices) {
            String toolName = service.getClass().getSimpleName().replace("Service", "").toLowerCase();
            toolServiceMap.put(toolName, service);
        }
    }

    public String collectDataFromTools(List<UserTool> activeTools) {
        StringBuilder report = new StringBuilder();

        for (UserTool userTool : activeTools) {
            String toolName = userTool.getTool().getToolName().toLowerCase().replaceAll("\\s", "");
            ToolDataService service = toolServiceMap.get(toolName);

            if (service != null) {
                report.append(service.fetchData()).append("\n");
            } else {
                report.append("No service available for tool: ").append(toolName).append("\n");
            }
        }

        return report.toString();
    }
}

