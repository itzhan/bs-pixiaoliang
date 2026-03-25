package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.PaymentRequest;
import com.parking.smart.entity.Payment;
import com.parking.smart.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付
     */
    @PostMapping
    public Result<Payment> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("创建支付");
        Payment payment = paymentService.createPayment(request);
        return Result.success(payment);
    }

    /**
     * 查询我的支付记录
     */
    @GetMapping("/my")
    public Result<PageResult<Payment>> getMyPayments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Payment> result = paymentService.getMyPayments(page, size);
        return Result.success(result);
    }

    /**
     * 查询所有支付记录（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Payment>> getAllPayments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        PageResult<Payment> result = paymentService.getAllPayments(page, size, status, keyword);
        return Result.success(result);
    }

    /**
     * 删除支付记录（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deletePayment(@PathVariable Long id) {
        log.info("管理员删除支付记录: id={}", id);
        paymentService.removeById(id);
        return Result.success();
    }
}
