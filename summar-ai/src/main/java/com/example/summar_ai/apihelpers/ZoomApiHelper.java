package com.example.summar_ai.apihelpers;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    // 🔹 Step 2: Get Messages for a Contact
    public String getChatMessages(String accessToken, String contactId, String startDate, String endDate) {
        String url = ZOOM_API_BASE_URL + "/chat/users/me/messages?to_contact=" + contactId + "&date=" + startDate +
                "&to=" + endDate;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            System.err.println("Failed to fetch messages for contact: " + contactId);
            return "";
        }
    }

    // Get chat channels
    public List<String> getChannels(String accessToken) {
        List<String> channelIds = new ArrayList<>();
        return channelIds;
    }

    // Get Messages from chat channels
    public String getChannelMessages(String accessToken, String channelId, String date) {
        return "";
    }

    // Get chat sessions report
    public List<String> getChatSessions(String accessToken, LocalDate startDate, LocalDate endDate) {
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
