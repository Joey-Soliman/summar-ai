package com.example.summar_ai.services.integrations;

import com.example.summar_ai.apihelpers.GoogleCalendarApiHelper;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.services.ToolDataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarService implements ToolDataService {

    private final GoogleCalendarApiHelper googleCalendarApiHelper;

    public GoogleCalendarService(GoogleCalendarApiHelper googleCalendarApiHelper) {
        this.googleCalendarApiHelper = googleCalendarApiHelper;
    }

    @Async
    @Override
    public String fetchData(UserTool userTool, String startDate, String endDate) {
        // Retrieve the access token for the authenticated user
        String accessToken = userTool.getAccessToken();

        // Fetch the Google Calendar data (you can customize what data you want)
        String calendarId = "primary"; // or get it from the userTool if needed
        String response = googleCalendarApiHelper.getGoogleCalendarData(accessToken, calendarId, startDate, endDate);

        // Process and return the data
        return response;
    }
}
