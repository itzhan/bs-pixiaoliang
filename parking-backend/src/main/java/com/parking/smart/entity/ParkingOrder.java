package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车订单
 */
@Data
@TableName("parking_order")
public class ParkingOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long vehicleId;

    private Long spaceId;

    private String plateNumber;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    /**
     * 停车时长（分钟）
     */
    private Integer duration;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态：0-进行中，1-已完成，2-已取消
     */
    private Integer status = 0;

    /**
     * 支付状态：0-未支付，1-已支付，2-已退款
     */
    private Integer paymentStatus = 0;

    private Long reservationId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
