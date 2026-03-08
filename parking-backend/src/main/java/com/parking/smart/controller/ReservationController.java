package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.ReservationRequest;
import com.parking.smart.entity.Reservation;
import com.parking.smart.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
     * 查询我的预约
     */
    @GetMapping("/my")
    public Result<PageResult<Reservation>> getMyReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        PageResult<Reservation> result = reservationService.getMyReservations(page, size, status);
        return Result.success(result);
    }

    /**
     * 查询所有预约（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Reservation>> getAllReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        PageResult<Reservation> result = reservationService.getAllReservations(page, size, status, keyword);
        return Result.success(result);
    }

    /**
     * 根据ID查询预约
     */
    @GetMapping("/{id}")
    public Result<Reservation> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return Result.success(reservation);
    }
}
