package com.parking.smart.controller;

import com.parking.smart.common.Result;
import com.parking.smart.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 查询仪表盘数据（管理员）
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = statisticsService.getDashboard();
        return Result.success(dashboard);
    }

    /**
     * 查询收入趋势（管理员）
     */
    @GetMapping("/revenue-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> getRevenueTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> trend = statisticsService.getRevenueTrend(days);
        return Result.success(trend);
    }

    /**
     * 查询时段统计（管理员）
     */
    @GetMapping("/hourly")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> getHourlyStats() {
        List<Map<String, Object>> stats = statisticsService.getHourlyStats();
        return Result.success(stats);
    }
}
