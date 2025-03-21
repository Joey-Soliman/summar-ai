package com.example.summar_ai.apihelpers;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleCalendarApiHelper {

    private final RestTemplate restTemplate;

    public GoogleCalendarApiHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getGoogleCalendarData(String accessToken, String calendarId, String startDate, String endDate) {
        // Construct the URL for the Google Calendar API (for example: getting events)
        // Example url: https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin=2025-03-12T00:00:00Z&timeMax=2025-03-19T23:59:59Z
        String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/events?timeMin=" + startDate
                + "&timeMax=" + endDate;
        // Set up the Authorization header with the Bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // Create the HttpEntity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make the GET request with headers, and parse the response into an array of GoogleCalendarEvent objects
        GoogleCalendarEvent[] events = restTemplate.exchange(url, HttpMethod.GET, entity, GoogleCalendarEvent[].class).getBody();

        // Make the API call and return the response
        return restTemplate.getForObject(url, String.class);
    }
}
