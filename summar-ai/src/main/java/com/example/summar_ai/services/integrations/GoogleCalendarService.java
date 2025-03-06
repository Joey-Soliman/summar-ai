package com.example.summar_ai.services.integrations;

import com.example.summar_ai.services.ToolDataService;
import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarService implements ToolDataService {
    @Override
    public String fetchData() {
        return "Google Calendar Data: [Upcoming meetings, events, etc.]";
    }
}
