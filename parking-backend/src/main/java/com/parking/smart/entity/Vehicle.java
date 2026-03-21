package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆信息
 */
@Data
@TableName("vehicle")
public class Vehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String plateNumber;

    /**
     * 车辆类型：SEDAN-轿车，SUV-越野车，TRUCK-货车，MOTORCYCLE-摩托车，OTHER-其他
     */
    private String vehicleType = "SEDAN";

    private String brand;

    private String color;

    /**
     * 是否默认车辆：0-否，1-是
     */
    private Integer isDefault = 0;

    /** 用户名（非持久化） */
    @TableField(exist = false)
    private String userName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
