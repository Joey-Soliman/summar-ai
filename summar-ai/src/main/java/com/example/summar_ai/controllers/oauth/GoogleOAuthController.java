package com.example.summar_ai.controllers.oauth;

import com.example.summar_ai.models.Tool;
import com.example.summar_ai.models.User;
import com.example.summar_ai.models.UserTool;
import com.example.summar_ai.repositories.ToolRepository;
import com.example.summar_ai.repositories.UserRepository;
import com.example.summar_ai.repositories.UserToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/oauth2")
public class GoogleOAuthController {

    @Autowired
    private UserToolRepository userToolRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/callback/google")
    public ResponseEntity<String> googleCallback(
            @RequestParam("code") String code,
            OAuth2AuthenticationToken authentication) {
        System.out.println("In GoogleOAuthController");

        // Get the authenticated user's ID
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the tool (Google Calendar)
        Tool tool = toolRepository.findByToolName("Google Calendar")
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        // Load OAuth2 client
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), username);

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

        return ResponseEntity.ok("Google OAuth successful!");
    }
}




