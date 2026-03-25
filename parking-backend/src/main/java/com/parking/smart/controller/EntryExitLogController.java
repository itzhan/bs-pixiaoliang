package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.entity.EntryExitLog;
import com.parking.smart.service.EntryExitLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 进出场记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/entry-exit-logs")
@RequiredArgsConstructor
public class EntryExitLogController {

    private final EntryExitLogService entryExitLogService;

    /**
     * 分页查询进出场记录（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<EntryExitLog>> getLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) String logType) {
        PageResult<EntryExitLog> result = entryExitLogService.getLogs(page, size, plateNumber, logType);
        return Result.success(result);
    }

    /**
     * 删除出入场记录（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteLog(@PathVariable Long id) {
        log.info("管理员删除出入场记录: id={}", id);
        entryExitLogService.removeById(id);
        return Result.success();
    }
}
