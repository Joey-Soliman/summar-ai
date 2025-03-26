package com.example.summar_ai.services.integrations;

import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.services.ToolDataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ZoomChatService implements ToolDataService {
    @Async
    @Override
    public String fetchData(UserTool userTool, String startDate, String endDate) {
        System.out.println("ZoomChatService fetching data...");
        String response = "Zoom Chat Data: [Recent chat messages, meetings, etc.]";
        return response;
    }
}
