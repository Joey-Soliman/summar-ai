package com.example.summar_ai.apihelpers;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JiraApiHelper {

    private final RestTemplate restTemplate = new RestTemplate();

    // In getUserIssuesForSite method
    public String getUserIssuesForSite(String accessToken, String siteUrl, String cloudId, LocalDate start, LocalDate end) {
        StringBuilder issuesString = new StringBuilder();

        String accountId = getAccountId(accessToken, siteUrl);
        String jql = String.format(
                "(creator = \"accountid:%s\" OR assignee = \"accountid:%s\" OR reporter = \"accountid:%s\") " +
                        "AND ((created >= \"%s\" AND created <= \"%s\") " +
                        "OR (updated >= \"%s\" AND updated <= \"%s\") " +
                        "OR (resolved >= \"%s\" AND resolved <= \"%s\"))",
                accountId, accountId, accountId,
                start.toString(), end.toString(),
                start, end,
                start, end
        );

        String url = siteUrl + "/rest/api/3/search?jql=" + UriUtils.encode(jql, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> issues = (List<Map<String, Object>>) body.get("issues");

            // Concatenate issues into a string
            for (Map<String, Object> issue : issues) {
                issuesString.append("Issue Key: ").append(issue.get("key")).append("\n");
                issuesString.append("Summary: ").append(issue.get("fields")).append("\n");
                issuesString.append("Description: ").append(issue.get("fields")).append("\n");
                // You can add more fields here as needed
                issuesString.append("\n-----\n");
            }
        }

        return issuesString.toString();
    }



    public String getAccountId(String accessToken, String siteUrl) {
        String url = siteUrl + "/rest/api/3/myself";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = response.getBody();
            return (String) body.get("accountId");  // assuming "accountId" is the key for the user's account ID
        } else {
            throw new RuntimeException("Failed to get account ID from Jira.");
        }
    }


    public List<Map<String, Object>> getAccessibleJiraSites(String accessToken) {
        String url = "https://api.atlassian.com/oauth/token/accessible-resources";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
        );

        return response.getBody(); // Each entry is a map with keys: id (cloudId), url, name, scopes, etc.
    }

}

