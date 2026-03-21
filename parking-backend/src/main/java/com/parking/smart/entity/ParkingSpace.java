package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车位
 */
@Data
@TableName("parking_space")
public class ParkingSpace {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long areaId;

    private String spaceNumber;

    /**
     * 车位类型：STANDARD-标准，VIP-VIP车位，DISABLED-残疾人车位，CHARGING-充电车位
     */
    private String spaceType = "STANDARD";

    /**
     * 状态：0-空闲，1-已占用，2-已预约，3-维护中
     */
    private Integer status = 0;

    /**
     * 每小时费率
     */
    private BigDecimal hourlyRate;

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
