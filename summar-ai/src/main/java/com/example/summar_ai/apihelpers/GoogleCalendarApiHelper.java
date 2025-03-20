package com.example.summar_ai.apihelpers;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleCalendarApiHelper {

    private final RestTemplate restTemplate;

    public GoogleCalendarApiHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getGoogleCalendarData(String accessToken, String calendarId) {
        // Construct the URL for the Google Calendar API (for example: getting events)
        // Example url: https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin=2025-03-12T00:00:00Z&timeMax=2025-03-19T23:59:59Z
        String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/events?access_token=" + accessToken;

        // Make the API call and return the response
        return restTemplate.getForObject(url, String.class);
    }
}
