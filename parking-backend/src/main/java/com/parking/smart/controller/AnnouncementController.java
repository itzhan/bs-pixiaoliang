package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.AnnouncementRequest;
import com.parking.smart.entity.Announcement;
import com.parking.smart.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 查询已发布的公告
     */
    @GetMapping("/published")
    public Result<List<Announcement>> getPublishedAnnouncements() {
        List<Announcement> announcements = announcementService.getPublishedAnnouncements();
        return Result.success(announcements);
    }

    /**
     * 分页查询公告（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Announcement>> getAnnouncements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        PageResult<Announcement> result = announcementService.getAnnouncements(page, size, status, keyword);
        return Result.success(result);
    }

    /**
     * 创建公告（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Announcement> createAnnouncement(@Valid @RequestBody AnnouncementRequest request) {
        log.info("创建公告: {}", request.getTitle());
        Announcement announcement = announcementService.createAnnouncement(request);
        return Result.success(announcement);
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateAnnouncement(@PathVariable Long id,
                                           @Valid @RequestBody AnnouncementRequest request) {
        log.info("更新公告: id={}", id);
        announcementService.updateAnnouncement(id, request);
        return Result.success();
    }

    /**
     * 发布公告（管理员）
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> publishAnnouncement(@PathVariable Long id) {
        log.info("发布公告: id={}", id);
        announcementService.publishAnnouncement(id);
        return Result.success();
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        log.info("删除公告: id={}", id);
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }
}
