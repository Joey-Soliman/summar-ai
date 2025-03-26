package com.example.summar_ai.services.integrations;

import net.minidev.json.JSONObject;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GPTService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api.key}") // Store API key in application.properties
    private String apiKey;

    public String summarizeReport(String reportData) {
        OkHttpClient client = new OkHttpClient();

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4"); // or "gpt-3.5-turbo" if you want a cheaper option
        requestBody.put("messages", new JSONObject[]{
                new JSONObject().put("role", "system").put("content", "You are an expert report summarizer."),
                new JSONObject().put("role", "user").put("content", "Summarize this report:\n" + reportData)
        });
        requestBody.put("max_tokens", 500);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                JSONObject jsonResponse = new JSONObject(response.body().string());
                return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Error: Unable to summarize report.";
    }
}

