package com.example.summar_ai.apihelpers;

import org.springframework.cglib.core.Local;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class GoogleCalendarApiHelper {

    private final RestTemplate restTemplate;

    public GoogleCalendarApiHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getGoogleCalendarData(String accessToken, String calendarId, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        ZonedDateTime startDateTime = startDate.atStartOfDay(timeZone);
        ZonedDateTime endDateTime = endDate.atTime(LocalTime.MAX).atZone(timeZone);
        DateTimeFormatter rfc3339Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        String formattedStart = startDateTime.format(rfc3339Formatter);
        String formattedEnd = endDateTime.format(rfc3339Formatter);
        // Construct the URL for the Google Calendar API (for example: getting events)
        // Example url: https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin=2025-03-12T00:00:00Z&timeMax=2025-03-19T23:59:59Z
        String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/events?timeMin=" + formattedStart
                + "&timeMax=" + formattedEnd;
        // Set up the Authorization header with the Bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // Create the HttpEntity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make the GET request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String jsonResponse = response.getBody();

        return jsonResponse;
    }
}
