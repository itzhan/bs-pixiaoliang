package com.parking.smart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.smart.entity.*;
import com.parking.smart.mapper.*;
import com.parking.smart.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserMapper userMapper;
    private final VehicleMapper vehicleMapper;
    private final ParkingAreaMapper parkingAreaMapper;
    private final ParkingOrderMapper parkingOrderMapper;
    private final PaymentMapper paymentMapper;
    private final ReservationMapper reservationMapper;
    private final EntryExitLogMapper entryExitLogMapper;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        // 总用户数
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        dashboard.put("totalUsers", totalUsers);

        // 总车辆数
        Long totalVehicles = vehicleMapper.selectCount(new LambdaQueryWrapper<>());
        dashboard.put("totalVehicles", totalVehicles);

        // 总车位数 & 可用车位数
        List<ParkingArea> areas = parkingAreaMapper.selectList(new LambdaQueryWrapper<>());
        int totalSpaces = areas.stream().mapToInt(a -> a.getTotalSpaces() != null ? a.getTotalSpaces() : 0).sum();
        int availableSpaces = areas.stream().mapToInt(a -> a.getAvailableSpaces() != null ? a.getAvailableSpaces() : 0).sum();
        dashboard.put("totalSpaces", totalSpaces);
        dashboard.put("availableSpaces", availableSpaces);

        // 占用率
        double occupancyRate = 0.0;
        if (totalSpaces > 0) {
            occupancyRate = BigDecimal.valueOf((totalSpaces - availableSpaces) * 100.0 / totalSpaces)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        dashboard.put("occupancyRate", occupancyRate);

        // 今日订单数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LambdaQueryWrapper<ParkingOrder> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.ge(ParkingOrder::getEntryTime, todayStart)
                .le(ParkingOrder::getEntryTime, todayEnd);
        Long todayOrders = parkingOrderMapper.selectCount(todayOrderWrapper);
        dashboard.put("todayOrders", todayOrders);

        // 今日收入
        LambdaQueryWrapper<Payment> todayPaymentWrapper = new LambdaQueryWrapper<>();
        todayPaymentWrapper.ge(Payment::getPaidAt, todayStart)
                .le(Payment::getPaidAt, todayEnd)
                .eq(Payment::getStatus, 1);
        List<Payment> todayPayments = paymentMapper.selectList(todayPaymentWrapper);
        BigDecimal todayRevenue = todayPayments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.put("todayRevenue", todayRevenue);

        // 活跃预约数（状态为 0-待使用 或 1-已使用）
        LambdaQueryWrapper<Reservation> activeResWrapper = new LambdaQueryWrapper<>();
        activeResWrapper.in(Reservation::getStatus, 0, 1);
        Long activeReservations = reservationMapper.selectCount(activeResWrapper);
        dashboard.put("activeReservations", activeReservations);

        // 总收入（所有已支付）
        LambdaQueryWrapper<Payment> allPaidWrapper = new LambdaQueryWrapper<>();
        allPaidWrapper.eq(Payment::getStatus, 1);
        List<Payment> allPaidPayments = paymentMapper.selectList(allPaidWrapper);
        BigDecimal totalRevenue = allPaidPayments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.put("totalRevenue", totalRevenue);

        // 本月收入
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LambdaQueryWrapper<Payment> monthPaymentWrapper = new LambdaQueryWrapper<>();
        monthPaymentWrapper.ge(Payment::getPaidAt, monthStart)
                .le(Payment::getPaidAt, todayEnd)
                .eq(Payment::getStatus, 1);
        List<Payment> monthPayments = paymentMapper.selectList(monthPaymentWrapper);
        BigDecimal monthRevenue = monthPayments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.put("monthRevenue", monthRevenue);

        return dashboard;
    }

    @Override
    public List<Map<String, Object>> getRevenueTrend(Integer days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Payment::getPaidAt, dayStart)
                    .le(Payment::getPaidAt, dayEnd)
                    .eq(Payment::getStatus, 1);
            List<Payment> payments = paymentMapper.selectList(wrapper);
            BigDecimal revenue = payments.stream()
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("revenue", revenue);
            trend.add(item);
        }

        return trend;
    }

    @Override
    public List<Map<String, Object>> getHourlyStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime hourStart = today.atTime(hour, 0, 0);
            LocalDateTime hourEnd = today.atTime(hour, 59, 59);

            LambdaQueryWrapper<EntryExitLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(EntryExitLog::getCreatedAt, hourStart)
                    .le(EntryExitLog::getCreatedAt, hourEnd)
                    .eq(EntryExitLog::getLogType, "ENTRY");
            Long count = entryExitLogMapper.selectCount(wrapper);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hour", hour);
            item.put("count", count);
            stats.add(item);
        }

        return stats;
    }
}
