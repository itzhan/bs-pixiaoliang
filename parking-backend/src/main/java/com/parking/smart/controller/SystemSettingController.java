package com.parking.smart.controller;

import com.parking.smart.common.Result;
import com.parking.smart.dto.SettingUpdateRequest;
import com.parking.smart.entity.SystemSetting;
import com.parking.smart.service.SystemSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统设置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    /**
     * 查询所有系统设置（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SystemSetting>> getAllSettings() {
        List<SystemSetting> settings = systemSettingService.getAllSettings();
        return Result.success(settings);
    }

    /**
     * 根据key查询设置值（管理员）
     */
    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> getSettingValue(@PathVariable String key) {
        String value = systemSettingService.getSettingValue(key);
        return Result.success(value);
    }

    /**
     * 批量更新系统设置（管理员）
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateSettings(@Valid @RequestBody SettingUpdateRequest request) {
        log.info("更新系统设置");
        systemSettingService.updateSettings(request.getSettings());
        return Result.success();
    }
}
