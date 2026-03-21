package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 黑名单
 */
@Data
@TableName("blacklist")
public class Blacklist {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String plateNumber;

    private String reason;

    /**
     * 状态：1-生效中，0-已解除
     */
    private Integer status = 1;

    private LocalDateTime blockedAt;

    private LocalDateTime removedAt;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /** 操作员名称（非持久化） */
    @TableField(exist = false)
    private String operatorName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
