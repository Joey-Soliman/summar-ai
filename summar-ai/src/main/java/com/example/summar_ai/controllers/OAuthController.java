package com.example.summar_ai.controllers;

import com.example.summar_ai.models.Tool;
import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.repositories.ToolRepository;
import com.example.summar_ai.repositories.UserRepository;
import com.example.summar_ai.repositories.UserToolRepository;
import com.example.summar_ai.services.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@Controller
public class OAuthController {

    @Autowired
    private UserToolRepository userToolRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private AuthService authService;

    @Autowired
    public OAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/oauth2/success")
    public String oauthCallback(OAuth2AuthenticationToken authentication,
                                @RequestParam(name = "toolName") String toolName,
                                HttpSession session) {
        System.out.println("OAuth Success for provider: " + authentication.getAuthorizedClientRegistrationId());

        // Try to restore original authentication from session
        if (session.getAttribute("ORIGINAL_AUTH") != null) {
            SecurityContextHolder.getContext().setAuthentication(
                    (org.springframework.security.core.Authentication) session.getAttribute("ORIGINAL_AUTH"));
        }
        User user = authService.getAuthenticatedUser();

        System.out.println("Authenticated User: " + (user != null ? user.getId() : "NULL"));

        // Find the tool dynamically based on the toolName passed in URL
        Tool tool = toolRepository.findByToolName(toolName)
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        // Load OAuth2 client
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        if (client != null) {
            String accessToken = client.getAccessToken().getTokenValue();
            String refreshToken = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;
            Instant expiresAt = client.getAccessToken().getExpiresAt();

            // Find or create UserTool entry
            UserTool userTool = userToolRepository.findByUserIdAndToolId(user.getId(), tool.getId())
                    .orElse(new UserTool());

            userTool.setUser(user);
            userTool.setTool(tool);
            userTool.setAccessToken(accessToken);
            userTool.setRefreshToken(refreshToken);
            userTool.setExpiresAt(expiresAt);

            // Save to the database
            userToolRepository.save(userTool);
        }
        System.out.println("OAuth Finish");

        // Redirect to user-tools page
        return "redirect:/user-tools";
    }
}
