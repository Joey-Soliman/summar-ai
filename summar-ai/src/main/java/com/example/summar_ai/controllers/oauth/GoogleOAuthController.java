package com.example.summar_ai.controllers.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GoogleOAuthController {
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;


    @GetMapping("/user-tools")
    public String googleRedirect(Model model, OAuth2AuthenticationToken authentication) {
        // This is where the user is redirected after successful authentication
        if (authentication != null) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(), authentication.getName());

            // Use the client to fetch details from Google Calendar API
            // For example, getting the Google Calendar events

            // Add data to model
            model.addAttribute("googleCalendarEvents", getGoogleCalendarEvents(client));
        }
        return "user-tools"; // The page to display user's Google Calendar events
    }
}

