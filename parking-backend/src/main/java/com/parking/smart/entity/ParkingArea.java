package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停车区域
 */
@Data
@TableName("parking_area")
public class ParkingArea {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String description;

    /**
     * 总车位数
     */
    private Integer totalSpaces;

    /**
     * 可用车位数
     */
    private Integer availableSpaces;

    /**
     * 楼层号
     */
    private Integer floorNumber;

    /**
     * 区域类型：INDOOR-室内，OUTDOOR-室外
     */
    private String areaType = "INDOOR";

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status = 1;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
