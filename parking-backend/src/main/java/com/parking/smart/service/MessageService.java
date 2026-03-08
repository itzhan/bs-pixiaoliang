package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.entity.Message;

public interface MessageService extends IService<Message> {

    PageResult<Message> getMessages(Integer page, Integer size, Integer isRead);

    Long getUnreadCount();

    void markAsRead(Long id);

    void markAllAsRead();

    void createMessage(Long userId, String title, String content, String messageType);
}
