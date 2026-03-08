package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    /**
     * 消息类型：SYSTEM-系统消息，ORDER-订单消息，PAYMENT-支付消息，OTHER-其他
     */
    private String messageType = "SYSTEM";

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
