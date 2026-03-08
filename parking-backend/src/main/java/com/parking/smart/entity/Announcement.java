package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告
 */
@Data
@TableName("announcement")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /**
     * 公告类型：NOTICE-通知，MAINTENANCE-维护，PROMOTION-促销，OTHER-其他
     */
    private String announcementType = "NOTICE";

    /**
     * 状态：0-草稿，1-已发布，2-已下架
     */
    private Integer status = 0;

    /**
     * 发布人ID
     */
    private Long publisherId;

    private LocalDateTime publishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
