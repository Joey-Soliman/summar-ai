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

        // Step 1: Get chat sessions
        List<String> sessionIds = zoomApiHelper.getChatSessions(userTool, startDate, endDate, timeZone);
        if (sessionIds == null || sessionIds.isEmpty()) {
            System.out.println("No contacts found.");
            return null;
        }
        System.out.println("Sessions: " + sessionIds);


        // Step 2: Fetch messages for each contact
        StringBuilder allMessages = new StringBuilder();
        for (String sessionId : sessionIds) {
            System.out.println("Looking for messages in session: " + sessionId);
            String messages = zoomApiHelper.getChatMessages(userTool, sessionId, startDate, endDate, timeZone);
            allMessages.append(messages).append("\n");
            System.out.println(messages);
        }

        return allMessages.toString();
    }
}
