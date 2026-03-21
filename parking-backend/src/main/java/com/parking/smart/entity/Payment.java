package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录
 */
@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String paymentNo;

    private Long orderId;

    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式：WECHAT-微信，ALIPAY-支付宝，CASH-现金，CARD-银行卡
     */
    private String paymentMethod = "WECHAT";

    /**
     * 支付状态：0-待支付，1-已支付，2-已退款，3-支付失败
     */
    private Integer status = 0;

    private LocalDateTime paidAt;

    private String remark;

    /** 订单号（非持久化） */
    @TableField(exist = false)
    private String orderNo;

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
