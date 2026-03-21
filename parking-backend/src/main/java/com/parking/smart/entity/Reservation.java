package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约记录
 */
@Data
@TableName("reservation")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String reservationNo;

    private Long userId;

    private Long vehicleId;

    private Long spaceId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 状态：0-待使用，1-已使用，2-已取消，3-已过期
     */
    private Integer status = 0;

    private String cancelReason;

    /**
     * 预约金额
     */
    private BigDecimal amount;

    /** 用户名（非持久化） */
    @TableField(exist = false)
    private String userName;

    /** 车牌号（非持久化） */
    @TableField(exist = false)
    private String plateNumber;

    /** 车位号（非持久化） */
    @TableField(exist = false)
    private String spaceNumber;

    /** 区域名称（非持久化） */
    @TableField(exist = false)
    private String areaName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
