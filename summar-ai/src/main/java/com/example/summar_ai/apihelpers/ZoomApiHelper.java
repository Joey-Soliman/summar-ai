package com.example.summar_ai.apihelpers;

import com.example.summar_ai.dto.ZoomTokenResponse;
import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class ZoomApiHelper {

    private static final String ZOOM_API_BASE_URL = "https://api.zoom.us/v2";
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.zoom.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.zoom.client-secret}")
    private String clientSecret;

    public ZoomApiHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getChatMessages(UserTool userTool, String targetId, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        List<String> allMessages = new ArrayList<>();

        for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
            try {
                // 🔹 Throttle API calls (sleep 1 second)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.err.println("Thread sleep interrupted during throttling: " + e.getMessage());
            }

            String formattedDate = formatDateInTimeZone(currentDate, timeZone);
            String url = ZOOM_API_BASE_URL + "/chat/users/me/messages?" + targetId + "&date=" + formattedDate;

            // Use the executeWithRetry method here
            String responseBody = executeWithRetry(userTool, accessToken -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken);
                headers.set("Content-Type", "application/json");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                } else {
                    System.err.println("Failed to fetch messages for recipient: " + targetId + " on " + formattedDate);
                    return null;
                }
            });

            if (responseBody != null) {
                allMessages.add(responseBody);
            }
        }

        return String.join("\n", allMessages);
    }


    // Helper function to format date in the correct time zone
    private String formatDateInTimeZone(LocalDate date, ZoneId timeZone) {
        // Convert LocalDate to ZonedDateTime in the user's time zone
        ZonedDateTime zonedDateTime = date.atStartOfDay(timeZone).withZoneSameInstant(ZoneOffset.UTC);

        // Format the date in the format Zoom API expects (ISO 8601)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        return zonedDateTime.format(formatter);
    }

    // Get chat sessions
    public List<String> getChatSessions(UserTool userTool, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        // Format dates
        String formattedStart = formatDateInTimeZone(startDate, timeZone);
        String formattedEnd = formatDateInTimeZone(endDate, timeZone);

        String url = ZOOM_API_BASE_URL + "/chat/users/me/sessions?from=" + formattedStart + "&to=" + formattedEnd;

        return executeWithRetry(userTool, accessToken -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Perform API call inside lambda
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                System.err.println("Failed to fetch sessions from Zoom. Status: " + response.getStatusCode());
                return Collections.emptyList();
            }

            List<Map<String, Object>> sessions = (List<Map<String, Object>>) response.getBody().get("sessions");
            List<String> contactIds = new ArrayList<>();

            for (Map<String, Object> session : sessions) {
                String contactId = getRecipientQueryParam(session, startDate, endDate, timeZone);
                if (contactId != null) {
                    contactIds.add(contactId);
                }
            }

            return contactIds;
        });
    }


    // Helper function parse session json object for getChatSessions function
    private String getRecipientQueryParam(Map<String, Object> session, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        String type = (String) session.get("type");
        String lastMessageTimeStr = (String) session.get("last_message_sent_time");

        if (lastMessageTimeStr == null || type == null) {
            return null;
        }

        ZonedDateTime utcTime = ZonedDateTime.parse(lastMessageTimeStr);

        // Convert to user's local time zone
        ZonedDateTime userTime = utcTime.withZoneSameInstant(timeZone);
        LocalDate messageDate = userTime.toLocalDate();

        if (messageDate.isBefore(startDate) || messageDate.isAfter(endDate)) {
            return null;
        }

        if ("1:1".equals(type)) {
            String email = (String) session.get("peer_contact_email");
            return email != null ? "to_contact=" + email : null;
        } else if ("groupchat".equals(type)) {
            String channelId = (String) session.get("channel_id");
            return channelId != null ? "to_channel=" + channelId : null;
        }

        return null;
    }


    public <T> T executeWithRetry(UserTool userTool, Function<String, T> apiCall) {
        try {
            return apiCall.apply(userTool.getAccessToken());
        } catch (HttpClientErrorException.Unauthorized e) {
            // Access token expired, refresh it
            String newAccessToken = refreshToken(userTool);
            userTool.setAccessToken(newAccessToken); // Save to DB if needed
            return apiCall.apply(newAccessToken);
        }
    }


    private String refreshToken(UserTool userTool) {
        String refreshToken = userTool.getRefreshToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=refresh_token&refresh_token=" + refreshToken;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<ZoomTokenResponse> response = restTemplate.postForEntity(
                    "https://zoom.us/oauth/token",
                    request,
                    ZoomTokenResponse.class
            );

            ZoomTokenResponse tokens = response.getBody();
            if (tokens != null) {
                userTool.setAccessToken(tokens.getAccessToken());
                userTool.setRefreshToken(tokens.getRefreshToken());
                // Calculate the expiration date
                Integer expiresIn = tokens.getExpiresIn(); // Get expires_in from the response
                if (expiresIn != null) {
                    Instant expiresAt = Instant.now().plusSeconds(expiresIn);
                    userTool.setExpiresAt(expiresAt); // Update expiration time
                }
                return tokens.getAccessToken();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Failed to refresh Zoom token");
    }

}
