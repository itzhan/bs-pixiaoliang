package com.parking.smart.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ReservationRequest;
import com.parking.smart.entity.Message;
import com.parking.smart.entity.ParkingArea;
import com.parking.smart.entity.ParkingSpace;
import com.parking.smart.entity.Reservation;
import com.parking.smart.entity.Vehicle;
import com.parking.smart.mapper.MessageMapper;
import com.parking.smart.mapper.ParkingSpaceMapper;
import com.parking.smart.mapper.ReservationMapper;
import com.parking.smart.mapper.VehicleMapper;
import com.parking.smart.service.ReservationService;
import com.parking.smart.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private com.parking.smart.mapper.PaymentMapper paymentMapper;

    @Autowired
    private com.parking.smart.service.BlacklistService blacklistService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation createReservation(ReservationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 检查用户的车辆是否在黑名单中
        Vehicle checkVehicle = vehicleMapper.selectById(request.getVehicleId());
        if (checkVehicle != null && blacklistService.isBlacklisted(checkVehicle.getPlateNumber())) {
            throw new BusinessException("该车辆已被列入黑名单，无法预约");
        }

        // 检查车位是否存在且状态为空闲
        ParkingSpace space = parkingSpaceMapper.selectById(request.getSpaceId());
        if (space == null) {
            throw new BusinessException("车位不存在");
        }
        if (space.getStatus() != 0) {
            throw new BusinessException("该车位当前不可预约");
        }

        // 检查车辆是否属于当前用户
        Vehicle vehicle = vehicleMapper.selectById(request.getVehicleId());
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        if (!vehicle.getUserId().equals(userId)) {
            throw new BusinessException("该车辆不属于当前用户");
        }

        // 检查同一车位同一时间段是否有冲突的预约
        Long conflictCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getSpaceId, request.getSpaceId())
                        .in(Reservation::getStatus, 0, 1) // 待使用或已使用
                        .lt(Reservation::getStartTime, request.getEndTime())
                        .gt(Reservation::getEndTime, request.getStartTime())
        );
        if (conflictCount > 0) {
            throw new BusinessException("该车位在所选时间段内已有预约");
        }

        // 生成预约编号
        String reservationNo = "RSV" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + RandomUtil.randomNumbers(4);

        // 自动计算金额（小时费率 × 时长）
        long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal amount = space.getHourlyRate().multiply(hours).setScale(2, RoundingMode.HALF_UP);

        // 创建预约
        Reservation reservation = new Reservation();
        reservation.setReservationNo(reservationNo);
        reservation.setUserId(userId);
        reservation.setVehicleId(request.getVehicleId());
        reservation.setSpaceId(request.getSpaceId());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setAmount(amount);
        reservation.setStatus(0); // 待使用
        reservation.setPaymentStatus(Boolean.TRUE.equals(request.getPayNow()) ? 1 : 0);
        baseMapper.insert(reservation);

        // 时段预约模式：不再改变车位本身的状态，车位保持空闲，直到全天所有时段被占满
        // 防止重复预约由 getBookedSlots 前端展示 + 后端时段冲突检测保证

        // 创建消息通知
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle("预约成功");
        message.setContent("您已成功预约车位 " + space.getSpaceNumber() + "，预约编号：" + reservationNo
                + "，金额：¥" + amount.toPlainString()
                + (Boolean.TRUE.equals(request.getPayNow()) ? "（已支付）" : "（待支付）"));
        message.setMessageType("ORDER");
        messageMapper.insert(message);

        log.info("用户 {} 成功创建预约 {}，金额={}", userId, reservationNo, amount);
        return reservation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long id, String reason) {
        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentRole();

        Reservation reservation = baseMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }

        // 检查权限：本人或管理员
        if (!"ADMIN".equals(role) && !reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该预约");
        }

        if (reservation.getStatus() != 0) {
            throw new BusinessException("只有待使用的预约才能取消");
        }

        // 更新预约状态为已取消
        reservation.setStatus(2);
        reservation.setCancelReason(reason);
        baseMapper.updateById(reservation);

        // 时段预约模式：取消预约不需要恢复车位状态（车位始终保持空闲）

        log.info("预约 {} 已取消，原因：{}", reservation.getReservationNo(), reason);
    }

    @Override
    public PageResult<Reservation> getMyReservations(Integer page, Integer size, Integer status, Integer paymentStatus) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(status != null, Reservation::getStatus, status)
                .eq(paymentStatus != null, Reservation::getPaymentStatus, paymentStatus)
                .orderByDesc(Reservation::getCreatedAt);

        IPage<Reservation> iPage = baseMapper.selectPage(new Page<>(page, size), wrapper);
        fillReservationNames(iPage.getRecords());
        return PageResult.from(iPage);
    }

    @Autowired
    private com.parking.smart.mapper.UserMapper userMapper;

    @Autowired
    private com.parking.smart.mapper.ParkingAreaMapper parkingAreaMapper;

    @Override
    public PageResult<Reservation> getAllReservations(Integer page, Integer size, Integer status, String keyword) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .eq(status != null, Reservation::getStatus, status)
                .like(StrUtil.isNotBlank(keyword), Reservation::getReservationNo, keyword)
                .orderByDesc(Reservation::getCreatedAt);

        IPage<Reservation> iPage = baseMapper.selectPage(new Page<>(page, size), wrapper);
        fillReservationNames(iPage.getRecords());
        return PageResult.from(iPage);
    }

    private void fillReservationNames(java.util.List<Reservation> list) {
        if (list == null || list.isEmpty()) return;
        // 用户名
        java.util.Set<Long> userIds = list.stream().map(Reservation::getUserId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, String> userNameMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userNameMap.put(u.getId(), u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }
        // 车辆车牌
        java.util.Set<Long> vehicleIds = list.stream().map(Reservation::getVehicleId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, String> plateMap = new java.util.HashMap<>();
        if (!vehicleIds.isEmpty()) {
            vehicleMapper.selectBatchIds(vehicleIds).forEach(v -> plateMap.put(v.getId(), v.getPlateNumber()));
        }
        // 车位号及区域
        java.util.Set<Long> spaceIds = list.stream().map(Reservation::getSpaceId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, ParkingSpace> spaceMap = new java.util.HashMap<>();
        java.util.Map<Long, String> areaNameMap = new java.util.HashMap<>();
        if (!spaceIds.isEmpty()) {
            java.util.List<ParkingSpace> spaces = parkingSpaceMapper.selectBatchIds(spaceIds);
            spaces.forEach(s -> spaceMap.put(s.getId(), s));
            java.util.Set<Long> areaIds = spaces.stream().map(ParkingSpace::getAreaId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
            if (!areaIds.isEmpty()) {
                parkingAreaMapper.selectBatchIds(areaIds).forEach(a -> areaNameMap.put(a.getId(), a.getName()));
            }
        }
        for (Reservation r : list) {
            r.setUserName(userNameMap.get(r.getUserId()));
            r.setPlateNumber(plateMap.get(r.getVehicleId()));
            ParkingSpace space = spaceMap.get(r.getSpaceId());
            if (space != null) {
                r.setSpaceNumber(space.getSpaceNumber());
                r.setAreaName(areaNameMap.get(space.getAreaId()));
            }
        }
    }

    @Override
    public Reservation getReservationById(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentRole();

        Reservation reservation = baseMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }

        // 检查权限：本人或管理员
        if (!"ADMIN".equals(role) && !reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该预约");
        }

        return reservation;
    }

    @Override
    public List<Map<String, String>> getBookedSlots(Long spaceId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<Reservation> reservations = baseMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getSpaceId, spaceId)
                        .in(Reservation::getStatus, 0, 1) // 待使用或已使用
                        .lt(Reservation::getStartTime, dayEnd)
                        .gt(Reservation::getEndTime, dayStart)
        );

        List<Map<String, String>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        for (Reservation r : reservations) {
            Map<String, String> slot = new HashMap<>();
            // 截取当天范围
            LocalDateTime start = r.getStartTime().isBefore(dayStart) ? dayStart : r.getStartTime();
            LocalDateTime end = r.getEndTime().isAfter(dayEnd) ? dayEnd : r.getEndTime();
            slot.put("start", start.format(fmt));
            slot.put("end", end.format(fmt));
            result.add(slot);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payReservation(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        Reservation reservation = baseMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该预约");
        }
        if (reservation.getPaymentStatus() != null && reservation.getPaymentStatus() == 1) {
            throw new BusinessException("该预约已支付");
        }

        reservation.setPaymentStatus(1);
        baseMapper.updateById(reservation);

        // 创建支付记录
        com.parking.smart.entity.Payment payment = new com.parking.smart.entity.Payment();
        payment.setPaymentNo("PAY" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + cn.hutool.core.util.RandomUtil.randomNumbers(4));
        payment.setUserId(userId);
        payment.setAmount(reservation.getAmount());
        payment.setPaymentMethod("WECHAT");
        payment.setStatus(1); // 已支付
        payment.setPaidAt(java.time.LocalDateTime.now());
        payment.setRemark("预约 " + reservation.getReservationNo() + " 支付");
        paymentMapper.insert(payment);

        // 创建消息通知
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle("支付成功");
        message.setContent("预约 " + reservation.getReservationNo() + " 已支付成功，金额：¥" + (reservation.getAmount() != null ? reservation.getAmount().toPlainString() : "0.00"));
        message.setMessageType("ORDER");
        messageMapper.insert(message);

        log.info("预约 {} 已支付", reservation.getReservationNo());
    }

    @Override
    public java.util.List<Reservation> getActiveReservationsByPlate(String plateNumber) {
        // 根据车牌号找到车辆
        java.util.List<Vehicle> vehicles = vehicleMapper.selectList(
                new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getPlateNumber, plateNumber)
        );
        if (vehicles.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.List<Long> vehicleIds = vehicles.stream().map(Vehicle::getId).collect(java.util.stream.Collectors.toList());

        // 查待使用的预约
        java.util.List<Reservation> list = baseMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .in(Reservation::getVehicleId, vehicleIds)
                        .eq(Reservation::getStatus, 0) // 待使用
                        .orderByAsc(Reservation::getStartTime)
        );
        fillReservationNames(list);
        return list;
    }

    @Override
    public void updateReservationStatus(Long id, Integer status) {
        Reservation reservation = baseMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        reservation.setStatus(status);
        baseMapper.updateById(reservation);
        log.info("管理员更新预约 {} 状态为 {}", reservation.getReservationNo(), status);
    }

    @Override
    public void deleteReservation(Long id) {
        Reservation reservation = baseMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        baseMapper.deleteById(id);
        log.info("管理员删除预约 {}", reservation.getReservationNo());
    }
}
