package com.example.summar_ai.services;

import com.example.summar_ai.models.UserTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportService {

    private final Map<String, ToolDataService> toolServiceMap;

    @Autowired
    public ReportService(List<ToolDataService> toolServices) {
        // Register all the tool services dynamically in a map
        toolServiceMap = new HashMap<>();
        for (ToolDataService service : toolServices) {
            String toolName = service.getClass().getSimpleName().replace("Service", "").toLowerCase();
            toolServiceMap.put(toolName, service);
        }
    }

    public CompletableFuture<String> collectDataFromTools(List<UserTool> activeTools, String startDate, String endDate) {
        // Create a list of CompletableFuture to run all the tool services concurrently
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (UserTool userTool : activeTools) {
            String toolName = userTool.getTool().getToolName().toLowerCase();
            ToolDataService service = toolServiceMap.get(toolName);

            if (service != null) {
                // Use @Async methods from the services
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> service.fetchData(userTool, startDate, endDate));
                futures.add(future);
            }
        }

        // Wait for all futures to complete and combine their results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    StringBuilder report = new StringBuilder();
                    for (CompletableFuture<String> future : futures) {
                        try {
                            report.append(future.get()).append("\n");
                        } catch (Exception e) {
                            report.append("Error fetching data\n");
                        }
                    }
                    return report.toString();
                });
    }
}
