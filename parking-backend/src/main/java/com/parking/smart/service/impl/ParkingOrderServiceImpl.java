package com.parking.smart.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.OrderEntryRequest;
import com.parking.smart.dto.OrderExitRequest;
import com.parking.smart.entity.*;
import com.parking.smart.mapper.*;
import com.parking.smart.service.BlacklistService;
import com.parking.smart.service.ParkingOrderService;
import com.parking.smart.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ParkingOrderServiceImpl extends ServiceImpl<ParkingOrderMapper, ParkingOrder> implements ParkingOrderService {

    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private EntryExitLogMapper entryExitLogMapper;

    @Autowired
    private ParkingAreaMapper parkingAreaMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private BillingRuleMapper billingRuleMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private BlacklistService blacklistService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingOrder vehicleEntry(OrderEntryRequest request) {
        String plateNumber = request.getPlateNumber();

        // 检查是否在黑名单中
        if (blacklistService.isBlacklisted(plateNumber)) {
            throw new BusinessException("该车牌已被列入黑名单，禁止入场");
        }

        // 检查车位是否存在
        ParkingSpace space = parkingSpaceMapper.selectById(request.getSpaceId());
        if (space == null) {
            throw new BusinessException("车位不存在");
        }

        // 查找车辆信息
        Vehicle vehicle = vehicleMapper.selectOne(
                new LambdaQueryWrapper<Vehicle>()
                        .eq(Vehicle::getPlateNumber, plateNumber)
                        .last("LIMIT 1")
        );

        Long userId = null;
        Long vehicleId = null;
        Long reservationId = null;

        if (vehicle != null) {
            userId = vehicle.getUserId();
            vehicleId = vehicle.getId();
        }

        // 检查车位状态
        if (space.getStatus() == 2) {
            // 车位已预约，检查预约是否匹配该车牌
            Reservation reservation = reservationMapper.selectOne(
                    new LambdaQueryWrapper<Reservation>()
                            .eq(Reservation::getSpaceId, space.getId())
                            .eq(Reservation::getStatus, 0) // 待使用
                            .last("LIMIT 1")
            );
            if (reservation != null) {
                // 检查预约车辆的车牌是否匹配
                Vehicle reservedVehicle = vehicleMapper.selectById(reservation.getVehicleId());
                if (reservedVehicle != null && !reservedVehicle.getPlateNumber().equals(plateNumber)) {
                    throw new BusinessException("该车位已被预约，且预约车辆与入场车辆不匹配");
                }
                // 预约匹配，更新预约状态为已完成
                reservationId = reservation.getId();
                reservation.setStatus(4); // 已完成（实际使用）
                reservationMapper.updateById(reservation);
            }
        } else if (space.getStatus() != 0) {
            throw new BusinessException("该车位当前不可使用");
        }

        // 生成订单编号
        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + RandomUtil.randomNumbers(4);

        // 创建订单
        ParkingOrder order = new ParkingOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setVehicleId(vehicleId);
        order.setSpaceId(space.getId());
        order.setPlateNumber(plateNumber);
        order.setEntryTime(LocalDateTime.now());
        order.setStatus(0); // 进行中
        order.setPaymentStatus(0); // 未支付
        order.setReservationId(reservationId);
        baseMapper.insert(order);

        // 更新车位状态为已占用
        space.setStatus(1);
        parkingSpaceMapper.updateById(space);

        // 创建入场记录
        EntryExitLog entryLog = new EntryExitLog();
        entryLog.setPlateNumber(plateNumber);
        entryLog.setLogType("ENTRY");
        entryLog.setOrderId(order.getId());
        entryExitLogMapper.insert(entryLog);

        // 更新区域可用车位数
        ParkingArea area = parkingAreaMapper.selectById(space.getAreaId());
        if (area != null && area.getAvailableSpaces() > 0) {
            area.setAvailableSpaces(area.getAvailableSpaces() - 1);
            parkingAreaMapper.updateById(area);
        }

        log.info("车辆 {} 入场，订单号：{}，车位：{}", plateNumber, orderNo, space.getSpaceNumber());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingOrder vehicleExit(OrderExitRequest request) {
        String plateNumber = request.getPlateNumber();

        // 查找进行中的订单
        ParkingOrder order = baseMapper.selectOne(
                new LambdaQueryWrapper<ParkingOrder>()
                        .eq(ParkingOrder::getPlateNumber, plateNumber)
                        .eq(ParkingOrder::getStatus, 0) // 进行中
                        .last("LIMIT 1")
        );
        if (order == null) {
            throw new BusinessException("未找到该车牌的进行中订单");
        }

        LocalDateTime now = LocalDateTime.now();

        // 计算停车时长（分钟）
        long durationMinutes = Duration.between(order.getEntryTime(), now).toMinutes();
        order.setDuration((int) durationMinutes);

        // 计算费用
        ParkingSpace space = parkingSpaceMapper.selectById(order.getSpaceId());
        BigDecimal amount = calculateAmount(space, (int) durationMinutes);
        order.setAmount(amount);

        // 更新订单
        order.setExitTime(now);
        order.setStatus(1); // 已完成
        baseMapper.updateById(order);

        // 恢复车位状态为空闲
        if (space != null) {
            space.setStatus(0);
            parkingSpaceMapper.updateById(space);

            // 更新区域可用车位数
            ParkingArea area = parkingAreaMapper.selectById(space.getAreaId());
            if (area != null) {
                area.setAvailableSpaces(area.getAvailableSpaces() + 1);
                parkingAreaMapper.updateById(area);
            }
        }

        // 创建出场记录
        EntryExitLog exitLog = new EntryExitLog();
        exitLog.setPlateNumber(plateNumber);
        exitLog.setLogType("EXIT");
        exitLog.setOrderId(order.getId());
        entryExitLogMapper.insert(exitLog);

        // 创建消息通知
        if (order.getUserId() != null) {
            Message message = new Message();
            message.setUserId(order.getUserId());
            message.setTitle("停车已完成");
            message.setContent("停车已完成，费用: ¥" + amount);
            message.setMessageType("ORDER");
            messageMapper.insert(message);
        }

        log.info("车辆 {} 出场，订单号：{}，时长：{}分钟，费用：{}", plateNumber, order.getOrderNo(), durationMinutes, amount);
        return order;
    }

    /**
     * 根据车位类型和停车时长计算费用
     */
    private BigDecimal calculateAmount(ParkingSpace space, int durationMinutes) {
        if (space == null) {
            return BigDecimal.ZERO;
        }

        // 优先查找对应车位类型的计费规则
        BillingRule rule = billingRuleMapper.selectOne(
                new LambdaQueryWrapper<BillingRule>()
                        .eq(BillingRule::getSpaceType, space.getSpaceType())
                        .eq(BillingRule::getIsActive, 1)
                        .last("LIMIT 1")
        );

        BigDecimal hourlyRate;
        int freeMinutes;
        BigDecimal dailyCap;

        if (rule != null) {
            hourlyRate = rule.getHourlyRate();
            freeMinutes = rule.getFreeMinutes() != null ? rule.getFreeMinutes() : 15;
            dailyCap = rule.getDailyCap();
        } else {
            // 使用车位自身的费率
            hourlyRate = space.getHourlyRate();
            freeMinutes = 15;
            dailyCap = null;
        }

        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 免费时段内不收费
        if (durationMinutes <= freeMinutes) {
            return BigDecimal.ZERO;
        }

        // 计算收费时长（超出免费时段的部分，按小时向上取整）
        int chargeableMinutes = durationMinutes - freeMinutes;
        int hours = (int) Math.ceil(chargeableMinutes / 60.0);
        BigDecimal amount = hourlyRate.multiply(BigDecimal.valueOf(hours));

        // 每日封顶
        if (dailyCap != null && dailyCap.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(dailyCap) > 0) {
            amount = dailyCap;
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PageResult<ParkingOrder> getMyOrders(Integer page, Integer size, Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getUserId, userId)
                .eq(status != null, ParkingOrder::getStatus, status)
                .orderByDesc(ParkingOrder::getCreatedAt);

        IPage<ParkingOrder> iPage = baseMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    public PageResult<ParkingOrder> getAllOrders(Integer page, Integer size, Integer status,
                                                 Integer paymentStatus, String keyword) {
        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<ParkingOrder>()
                .eq(status != null, ParkingOrder::getStatus, status)
                .eq(paymentStatus != null, ParkingOrder::getPaymentStatus, paymentStatus)
                .and(StrUtil.isNotBlank(keyword), w -> w
                        .like(ParkingOrder::getOrderNo, keyword)
                        .or()
                        .like(ParkingOrder::getPlateNumber, keyword)
                )
                .orderByDesc(ParkingOrder::getCreatedAt);

        IPage<ParkingOrder> iPage = baseMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    public ParkingOrder getOrderById(Long id) {
        ParkingOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAbnormal(Long id) {
        ParkingOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(2); // 异常
        baseMapper.updateById(order);
        log.info("订单 {} 已标记为异常", order.getOrderNo());
    }
}
