package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ReservationRequest;
import com.parking.smart.entity.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    PageResult<Reservation> getMyReservations(Integer page, Integer size, Integer status, Integer paymentStatus);

    /**
     * 管理员分页查询所有预约
     */
    PageResult<Reservation> getAllReservations(Integer page, Integer size, Integer status, String keyword);

    /**
     * 根据ID查询预约详情
     */
    Reservation getReservationById(Long id);

    /**
     * 查询某车位某日已被预约的时间段
     */
    List<Map<String, String>> getBookedSlots(Long spaceId, LocalDate date);

    /**
     * 根据车牌号查询待使用的预约
     */
    List<Reservation> getActiveReservationsByPlate(String plateNumber);

    /**
     * 支付预约
     */
    void payReservation(Long id);

    /**
     * 管理员更新预约状态
     */
    void updateReservationStatus(Long id, Integer status);

    /**
     * 管理员删除预约
     */
    void deleteReservation(Long id);
}

