package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ReservationRequest;
import com.parking.smart.entity.Reservation;

public interface ReservationService extends IService<Reservation> {

    /**
     * 创建预约
     */
    Reservation createReservation(ReservationRequest request);

    /**
     * 取消预约
     */
    void cancelReservation(Long id, String reason);

    /**
     * 分页查询当前用户的预约
     */
    PageResult<Reservation> getMyReservations(Integer page, Integer size, Integer status);

    /**
     * 管理员分页查询所有预约
     */
    PageResult<Reservation> getAllReservations(Integer page, Integer size, Integer status, String keyword);

    /**
     * 根据ID查询预约详情
     */
    Reservation getReservationById(Long id);
}
