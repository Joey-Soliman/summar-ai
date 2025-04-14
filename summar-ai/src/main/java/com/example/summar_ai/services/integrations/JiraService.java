package com.example.summar_ai.services.integrations;

import com.example.summar_ai.apihelpers.JiraApiHelper;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.services.ToolDataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class JiraService implements ToolDataService {
    private final JiraApiHelper jiraApiHelper;

    public JiraService(JiraApiHelper jiraApiHelper) {this.jiraApiHelper = jiraApiHelper;}

    @Async
    @Override
    public String fetchData(UserTool userTool, LocalDate startDate, LocalDate endDate, ZoneId timeZone) {
        // Get access token
        String accessToken = userTool.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("Error: Access token is missing for Jira.");
            return null;
        }

        // Get accessible Jira sites
        List<Map<String, Object>> sites = jiraApiHelper.getAccessibleJiraSites(accessToken);

        StringBuilder allIssues = new StringBuilder();
        for (Map<String, Object> site : sites) {
            String siteUrl = (String) site.get("url");
            String cloudId = (String) site.get("id");

            try {
                // Get issues for this site within the date range (returns a single string)
                String issues = jiraApiHelper.getUserIssuesForSite(accessToken, siteUrl, cloudId, startDate, endDate);
                allIssues.append(issues).append("\n");  // Append the string to the result
            } catch (Exception e) {
                System.err.println("Failed to get issues for site " + siteUrl + ": " + e.getMessage());
            }
        }

        return allIssues.toString();
    }


}

