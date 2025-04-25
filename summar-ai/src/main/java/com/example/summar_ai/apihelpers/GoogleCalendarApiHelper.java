package com.example.summar_ai.apihelpers;

import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.repositories.UserToolRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
public class GoogleCalendarApiHelper {

    private final RestTemplate restTemplate;
    private final UserToolRepository userToolRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public GoogleCalendarApiHelper(RestTemplate restTemplate, UserToolRepository userToolRepository) {
        this.restTemplate = restTemplate;
        this.userToolRepository = userToolRepository;
    }

    public String getGoogleCalendarData(UserTool userTool, String calendarId, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        String accessToken = userTool.getAccessToken();
        String refreshToken = userTool.getRefreshToken();

        try {
            if (userTool.getExpiresAt().isBefore(Instant.now().plusSeconds(60))) {
                System.out.println("Access token expired or near expiry. Refreshing...");
                List<String> tokenData = refreshAccessToken(refreshToken);
                String newAccessToken = tokenData.get(0);
                System.out.println("New access token: " + newAccessToken);
                userTool.setAccessToken(newAccessToken);
                userTool.setExpiresAt(Instant.parse(tokenData.get(1)));
                userToolRepository.save(userTool);
            }
            return fetchCalendarData(userTool, calendarId, startDate, endDate, timeZone);
        } catch (HttpClientErrorException.Unauthorized e) {
            System.out.println("refreshing token");
            // Refresh and retry once
            String newAccessToken = refreshAccessToken(refreshToken).get(0);

            // Save updated access token
            userTool.setAccessToken(newAccessToken);
            userTool.setExpiresAt(Instant.now().plusSeconds(3600)); // Google tokens are usually 1 hour
            userToolRepository.save(userTool);

            return fetchCalendarData(userTool, calendarId, startDate, endDate, timeZone);
        }
    }

    private String fetchCalendarData(UserTool userTool, String calendarId, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        ZonedDateTime startDateTime = startDate.atStartOfDay(timeZone);
        ZonedDateTime endDateTime = endDate.atTime(LocalTime.MAX).atZone(timeZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        String formattedStart = startDateTime.format(formatter);
        String formattedEnd = endDateTime.format(formatter);

        String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendarId +
                "/events?timeMin=" + formattedStart + "&timeMax=" + formattedEnd;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + userTool.getAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Token likely expired, try refreshing it
                List<String> tokens = refreshAccessToken(userTool.getRefreshToken()); // implement this however your app works
                String newAccessToken = tokens.get(0);
                // Optionally update the saved token in your DB

                // Retry the request with new token
                headers.set("Authorization", "Bearer " + newAccessToken);
                entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                return response.getBody();
            } else {
                throw e; // rethrow if it's not a 401
            }
        }
    }

    private List<String> refreshAccessToken(String refreshToken) {
        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        Map<String, Object> responseBody = response.getBody();



        if (responseBody != null && responseBody.containsKey("access_token") && responseBody.containsKey("expires_in")) {
            String accessToken = (String) responseBody.get("access_token");
            Integer expiresIn = (Integer) responseBody.get("expires_in");
            Instant expiresAt = Instant.now().plusSeconds(expiresIn);
            return Arrays.asList(accessToken, expiresAt.toString());
        } else {
            throw new RuntimeException("Failed to refresh access token: " + responseBody);
        }
    }

}
