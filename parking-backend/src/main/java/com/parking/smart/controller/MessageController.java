package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.entity.Message;
import com.parking.smart.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 消息控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 分页查询消息
     */
    @GetMapping
    public Result<PageResult<Message>> getMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer isRead) {
        PageResult<Message> result = messageService.getMessages(page, size, isRead);
        return Result.success(result);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long count = messageService.getUnreadCount();
        return Result.success(count);
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        messageService.markAllAsRead();
        return Result.success();
    }
}
