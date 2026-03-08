package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.PaymentRequest;
import com.parking.smart.entity.Payment;

public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付
     */
    Payment createPayment(PaymentRequest request);

    /**
     * 分页查询当前用户的支付记录
     */
    PageResult<Payment> getMyPayments(Integer page, Integer size);

    /**
     * 管理员分页查询所有支付记录
     */
    PageResult<Payment> getAllPayments(Integer page, Integer size, Integer status, String keyword);
}
