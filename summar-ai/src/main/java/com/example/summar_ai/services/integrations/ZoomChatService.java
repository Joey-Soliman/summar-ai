package com.example.summar_ai.services.integrations;

import com.example.summar_ai.apihelpers.ZoomApiHelper;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.services.ToolDataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class ZoomChatService implements ToolDataService {

    private final ZoomApiHelper zoomApiHelper;

    public ZoomChatService(ZoomApiHelper zoomApiHelper) {
        this.zoomApiHelper = zoomApiHelper;
    }

    @Async
    @Override
    public String fetchData(UserTool userTool, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        System.out.println("ZoomChatService fetching data...");

        // Get access token
        String accessToken = userTool.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("Error: Access token is missing for Zoom.");
            return null;
        }

        // Step 1: Get chat sessions
        List<String> sessionIds = zoomApiHelper.getChatSessions(accessToken, startDate, endDate, timeZone);
        if (sessionIds == null || sessionIds.isEmpty()) {
            System.out.println("No contacts found.");
            return null;
        }
        System.out.println("Sessions: " + sessionIds);


        // Step 2: Fetch messages for each contact
        StringBuilder allMessages = new StringBuilder();
        for (String sessionId : sessionIds) {
            System.out.println("Looking for messages in session: " + sessionId);
            try {
                // 🔹 Throttle API calls (sleep 500ms)
                Thread.sleep(1000);
                String messages = zoomApiHelper.getChatMessages(accessToken, sessionId, startDate, endDate, timeZone);
                allMessages.append(messages).append("\n");
                System.out.println(messages);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.err.println("Thread sleep interrupted: " + e.getMessage());
            }
        }

        return allMessages.toString();
    }
}
