package com.example.summar_ai.services;

import com.example.summar_ai.models.UserTool;

public interface ToolDataService {
    String fetchData(UserTool userTool, String startDate, String endDate);
}
