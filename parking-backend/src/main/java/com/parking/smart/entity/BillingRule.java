package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计费规则
 */
@Data
@TableName("billing_rule")
public class BillingRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 适用车位类型
     */
    private String spaceType;

    /**
     * 每小时费率
     */
    private BigDecimal hourlyRate;

    /**
     * 每日封顶金额
     */
    private BigDecimal dailyCap;

    /**
     * 免费时长（分钟）
     */
    private Integer freeMinutes = 15;

    /**
     * 是否启用：1-启用，0-禁用
     */
    private Integer isActive = 1;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
