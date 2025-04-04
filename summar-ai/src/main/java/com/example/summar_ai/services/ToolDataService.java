package com.example.summar_ai.services;

import com.example.summar_ai.models.UserTool;

import java.time.LocalDate;
import java.time.ZoneId;

public interface ToolDataService {
    String fetchData(UserTool userTool, LocalDate startDate, LocalDate endDate, ZoneId timeZone);
}
