package com.example.summar_ai.apihelpers;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Component
public class GPTApiHelper {
    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public GPTApiHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String callGPT(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openAiApiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an AI assistant."),
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 100,
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, Map.class);

        Map response = responseEntity.getBody();
        if (response == null || !response.containsKey("choices")) {
            return "Error: Invalid response from OpenAI";
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            return "No response";
        }

        // Extract "content" from the "message" object inside "choices"
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

        if (message == null || !message.containsKey("content")) {
            return "Error: No content in response";
        }

        return message.get("content").toString().trim();
    }
}

