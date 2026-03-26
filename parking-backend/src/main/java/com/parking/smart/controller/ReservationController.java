package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.ReservationRequest;
import com.parking.smart.entity.Reservation;
import com.parking.smart.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 创建预约
     */
    @PostMapping
    public Result<Reservation> createReservation(@Valid @RequestBody ReservationRequest request) {
        log.info("创建预约");
        Reservation reservation = reservationService.createReservation(request);
        return Result.success(reservation);
    }

    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id,
                                          @RequestParam(required = false) String reason) {
        log.info("取消预约: id={}", id);
        reservationService.cancelReservation(id, reason);
        return Result.success();
    }

    /**
     * 查询某车位某日已被预约的时间段
     */
    @GetMapping("/available-slots")
    public Result<List<Map<String, String>>> getAvailableSlots(
            @RequestParam Long spaceId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("查询可用时段: spaceId={}, date={}", spaceId, date);
        List<Map<String, String>> bookedSlots = reservationService.getBookedSlots(spaceId, date);
        return Result.success(bookedSlots);
    }

    /**
     * 支付预约
     */
    @PutMapping("/{id}/pay")
    public Result<Void> payReservation(@PathVariable Long id) {
        log.info("支付预约: id={}", id);
        reservationService.payReservation(id);
        return Result.success();
    }

    /**
     * 查询我的预约
     */
    @GetMapping("/my")
    public Result<PageResult<Reservation>> getMyReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer paymentStatus) {
        PageResult<Reservation> result = reservationService.getMyReservations(page, size, status, paymentStatus);
        return Result.success(result);
    }

    /**
     * 查询所有预约（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<PageResult<Reservation>> getAllReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        PageResult<Reservation> result = reservationService.getAllReservations(page, size, status, keyword);
        return Result.success(result);
    }

    /**
     * 根据车牌号查询待使用的预约（管理员 - 入场时使用）
     */
    @GetMapping("/by-plate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<List<Reservation>> getReservationsByPlate(@RequestParam String plateNumber) {
        List<Reservation> list = reservationService.getActiveReservationsByPlate(plateNumber);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Reservation> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return Result.success(reservation);
    }

    /**
     * 管理员更新预约状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<Void> updateReservationStatus(@PathVariable Long id,
                                                 @RequestParam Integer status) {
        log.info("管理员更新预约状态: id={}, status={}", id, status);
        reservationService.updateReservationStatus(id, status);
        return Result.success();
    }

    /**
     * 管理员删除预约
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteReservation(@PathVariable Long id) {
        log.info("管理员删除预约: id={}", id);
        reservationService.deleteReservation(id);
        return Result.success();
    }
}

