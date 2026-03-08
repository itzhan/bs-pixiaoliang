package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.OrderEntryRequest;
import com.parking.smart.dto.OrderExitRequest;
import com.parking.smart.entity.ParkingOrder;
import com.parking.smart.service.ParkingOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 停车订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ParkingOrderController {

    private final ParkingOrderService parkingOrderService;

    /**
     * 车辆入场
     */
    @PostMapping("/entry")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<ParkingOrder> vehicleEntry(@Valid @RequestBody OrderEntryRequest request) {
        log.info("车辆入场: {}", request.getPlateNumber());
        ParkingOrder order = parkingOrderService.vehicleEntry(request);
        return Result.success(order);
    }

    /**
     * 车辆出场
     */
    @PostMapping("/exit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<ParkingOrder> vehicleExit(@Valid @RequestBody OrderExitRequest request) {
        log.info("车辆出场");
        ParkingOrder order = parkingOrderService.vehicleExit(request);
        return Result.success(order);
    }

    /**
     * 查询我的订单
     */
    @GetMapping("/my")
    public Result<PageResult<ParkingOrder>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        PageResult<ParkingOrder> result = parkingOrderService.getMyOrders(page, size, status);
        return Result.success(result);
    }

    /**
     * 查询所有订单（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ParkingOrder>> getAllOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) String keyword) {
        PageResult<ParkingOrder> result = parkingOrderService.getAllOrders(page, size, status, paymentStatus, keyword);
        return Result.success(result);
    }

    /**
     * 根据ID查询订单
     */
    @GetMapping("/{id}")
    public Result<ParkingOrder> getOrderById(@PathVariable Long id) {
        ParkingOrder order = parkingOrderService.getOrderById(id);
        return Result.success(order);
    }

    /**
     * 标记异常订单（管理员）
     */
    @PutMapping("/{id}/abnormal")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> markAbnormal(@PathVariable Long id) {
        log.info("标记异常订单: id={}", id);
        parkingOrderService.markAbnormal(id);
        return Result.success();
    }
}
