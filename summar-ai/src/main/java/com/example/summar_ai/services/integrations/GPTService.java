package com.example.summar_ai.services.integrations;

import com.example.summar_ai.apihelpers.GPTApiHelper;
import org.springframework.stereotype.Service;

@Service
public class GPTService {
    private final GPTApiHelper GPTApiHelper;

    public GPTService(GPTApiHelper GPTApiHelper) {
        this.GPTApiHelper = GPTApiHelper;
    }

    public String summarizeReport(String reportText) {
        // String prompt = "Summarize the following:\n" + reportText;
        String prompt = "Hi how are you?";
        return GPTApiHelper.callGPT(prompt);
    }
}


