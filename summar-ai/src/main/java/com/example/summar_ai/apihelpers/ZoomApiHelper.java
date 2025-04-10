package com.example.summar_ai.apihelpers;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ZoomApiHelper {

    private static final String ZOOM_API_BASE_URL = "https://api.zoom.us/v2";
    private final RestTemplate restTemplate;

    public ZoomApiHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 🔹 Step 1: Get Contact List
    public List<String> getContacts(String accessToken) {
        String url = ZOOM_API_BASE_URL + "/chat/users/me/contacts?type=external";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<String> contactIds = new ArrayList<>();
            List<Map<String, Object>> contacts = (List<Map<String, Object>>) response.getBody().get("contacts");
            for (Map<String, Object> contact : contacts) {
                contactIds.add((String) contact.get("id"));
            }
            return contactIds;
        } else {
            System.err.println("Failed to fetch contacts from Zoom.");
            return null;
        }
    }

    public String getChatMessages(String accessToken, String targetId, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
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

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    allMessages.add(response.getBody());
                } else {
                    System.err.println("Failed to fetch messages for recipient: " + targetId + " on " + formattedDate);
                }
            } catch (Exception ex) {
                System.err.println("Exception fetching messages for " + targetId + " on " + formattedDate + ": " + ex.getMessage());
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
    public List<String> getChatSessions(String accessToken, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        // Format dates
        String formattedStart = formatDateInTimeZone(startDate, timeZone);
        String formattedEnd = formatDateInTimeZone(endDate, timeZone);

        String url = ZOOM_API_BASE_URL + "/chat/users/me/sessions?from=" + formattedStart + "&to=" + formattedEnd;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
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


    // Get chat sessions report
    public List<String> getSessionsReport(String accessToken, LocalDate startDate, LocalDate endDate) {
        String url = ZOOM_API_BASE_URL + "/report/chat/sessions?from=" + formatDate(startDate) + "&to=" + formatDate(endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<String> sessionIds = new ArrayList<>();
            List<Map<String, Object>> sessions = (List<Map<String, Object>>) response.getBody().get("sessions");
            for (Map<String, Object> session : sessions) {
                sessionIds.add((String) session.get("id"));
            }
            return sessionIds;
        } else {
            System.err.println("Failed to fetch contacts from Zoom.");
            return null;
        }
    }

    // Get chat messages report
    public String getMessageReport(String accessToken, String sessionId, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        String url = ZOOM_API_BASE_URL + "/report/chat/sessions/" + sessionId + "?from=" + formatDate(startDate) + "&to=" + formatDate(endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            System.err.println("Failed to fetch messages for contact: " + sessionId);
            return "";
        }
    }

    // Format date
    public String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
}
