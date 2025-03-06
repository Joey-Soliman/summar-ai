package com.example.summar_ai.services.integrations;

import com.example.summar_ai.services.ToolDataService;
import org.springframework.stereotype.Service;

@Service
public class ZoomChatService implements ToolDataService {
    @Override
    public String fetchData() {
        return "Zoom Chat Data: [Recent chat messages, meetings, etc.]";
    }
}
