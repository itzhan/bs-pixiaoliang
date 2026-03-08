package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.OrderEntryRequest;
import com.parking.smart.dto.OrderExitRequest;
import com.parking.smart.entity.ParkingOrder;

public interface ParkingOrderService extends IService<ParkingOrder> {

    /**
     * 车辆入场
     */
    ParkingOrder vehicleEntry(OrderEntryRequest request);

    /**
     * 车辆出场
     */
    ParkingOrder vehicleExit(OrderExitRequest request);

    /**
     * 分页查询当前用户的订单
     */
    PageResult<ParkingOrder> getMyOrders(Integer page, Integer size, Integer status);

    /**
     * 管理员分页查询所有订单
     */
    PageResult<ParkingOrder> getAllOrders(Integer page, Integer size, Integer status, Integer paymentStatus, String keyword);

    /**
     * 根据ID查询订单详情
     */
    ParkingOrder getOrderById(Long id);

    /**
     * 标记订单为异常
     */
    void markAbnormal(Long id);
}
