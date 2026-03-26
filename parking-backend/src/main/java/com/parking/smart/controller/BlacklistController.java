package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.BlacklistRequest;
import com.parking.smart.entity.Blacklist;
import com.parking.smart.service.BlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 黑名单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    /**
     * 分页查询黑名单（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<PageResult<Blacklist>> getBlacklist(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        PageResult<Blacklist> result = blacklistService.getBlacklist(page, size, status, keyword);
        return Result.success(result);
    }

    /**
     * 添加到黑名单（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<Blacklist> addToBlacklist(@Valid @RequestBody BlacklistRequest request) {
        log.info("添加到黑名单: {}", request.getPlateNumber());
        Blacklist blacklist = blacklistService.addToBlacklist(request);
        return Result.success(blacklist);
    }

    /**
     * 从黑名单移除（管理员）
     */
    @PutMapping("/{id}/remove")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<Void> removeFromBlacklist(@PathVariable Long id) {
        log.info("从黑名单移除: id={}", id);
        blacklistService.removeFromBlacklist(id);
        return Result.success();
    }
}
