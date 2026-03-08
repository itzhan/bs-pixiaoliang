package com.parking.smart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.entity.Message;
import com.parking.smart.mapper.MessageMapper;
import com.parking.smart.service.MessageService;
import com.parking.smart.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public PageResult<Message> getMessages(Integer page, Integer size, Integer isRead) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }
        wrapper.orderByDesc(Message::getCreatedAt);
        IPage<Message> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0);
        return count(wrapper);
    }

    @Override
    public void markAsRead(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Message message = getById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该消息");
        }
        message.setIsRead(1);
        updateById(message);
        log.info("消息标记已读, id={}", id);
    }

    @Override
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1);
        update(wrapper);
        log.info("全部消息标记已读, userId={}", userId);
    }

    @Override
    public void createMessage(Long userId, String title, String content, String messageType) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setMessageType(messageType);
        save(message);
        log.info("消息创建成功, id={}, userId={}, type={}", message.getId(), userId, messageType);
    }
}
