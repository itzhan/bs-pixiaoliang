package com.parking.smart.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getDashboard();

    List<Map<String, Object>> getRevenueTrend(Integer days);

    List<Map<String, Object>> getHourlyStats();
}
