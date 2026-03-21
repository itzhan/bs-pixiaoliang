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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation createReservation(ReservationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
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

        // 创建预约
        Reservation reservation = new Reservation();
        reservation.setReservationNo(reservationNo);
        reservation.setUserId(userId);
        reservation.setVehicleId(request.getVehicleId());
        reservation.setSpaceId(request.getSpaceId());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(0); // 待使用
        baseMapper.insert(reservation);

        // 更新车位状态为已预约
        space.setStatus(2);
        parkingSpaceMapper.updateById(space);

        // 同步更新区域可用车位数
        ParkingArea area = parkingAreaMapper.selectById(space.getAreaId());
        if (area != null && area.getAvailableSpaces() != null && area.getAvailableSpaces() > 0) {
            area.setAvailableSpaces(area.getAvailableSpaces() - 1);
            parkingAreaMapper.updateById(area);
        }

        // 创建消息通知
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle("预约成功");
        message.setContent("您已成功预约车位 " + space.getSpaceNumber() + "，预约编号：" + reservationNo);
        message.setMessageType("ORDER");
        messageMapper.insert(message);

        log.info("用户 {} 成功创建预约 {}", userId, reservationNo);
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

        // 恢复车位状态为空闲
        ParkingSpace space = parkingSpaceMapper.selectById(reservation.getSpaceId());
        if (space != null) {
            space.setStatus(0);
            parkingSpaceMapper.updateById(space);

            // 同步更新区域可用车位数
            ParkingArea area = parkingAreaMapper.selectById(space.getAreaId());
            if (area != null) {
                area.setAvailableSpaces((area.getAvailableSpaces() != null ? area.getAvailableSpaces() : 0) + 1);
                parkingAreaMapper.updateById(area);
            }
        }

        log.info("预约 {} 已取消，原因：{}", reservation.getReservationNo(), reason);
    }

    @Override
    public PageResult<Reservation> getMyReservations(Integer page, Integer size, Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(status != null, Reservation::getStatus, status)
                .orderByDesc(Reservation::getCreatedAt);

        IPage<Reservation> iPage = baseMapper.selectPage(new Page<>(page, size), wrapper);
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
}
