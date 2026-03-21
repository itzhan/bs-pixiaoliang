package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.VehicleRequest;
import com.parking.smart.entity.Vehicle;
import com.parking.smart.mapper.VehicleMapper;
import com.parking.smart.service.VehicleService;
import com.parking.smart.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    @Override
    public List<Vehicle> getMyVehicles() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return list(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getUserId, currentUserId)
                .orderByDesc(Vehicle::getIsDefault)
                .orderByDesc(Vehicle::getCreatedAt)
        );
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.parking.smart.mapper.UserMapper userMapper;

    @Override
    public PageResult<Vehicle> getVehicles(Integer page, Integer size, String keyword, Long userId) {
        Page<Vehicle> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索：车牌号、品牌
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(Vehicle::getPlateNumber, keyword)
                    .or().like(Vehicle::getBrand, keyword)
            );
        }

        // 用户ID筛选
        if (userId != null) {
            wrapper.eq(Vehicle::getUserId, userId);
        }

        wrapper.orderByDesc(Vehicle::getCreatedAt);

        Page<Vehicle> result = page(pageParam, wrapper);
        fillVehicleNames(result.getRecords());
        return PageResult.from(result);
    }

    private void fillVehicleNames(java.util.List<Vehicle> list) {
        if (list == null || list.isEmpty()) return;
        java.util.Set<Long> userIds = list.stream().map(Vehicle::getUserId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        if (userIds.isEmpty()) return;
        java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
        userMapper.selectBatchIds(userIds).forEach(u -> nameMap.put(u.getId(), u.getRealName() != null ? u.getRealName() : u.getUsername()));
        list.forEach(v -> v.setUserName(nameMap.get(v.getUserId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Vehicle addVehicle(VehicleRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        // 检查车牌号是否已存在
        Long count = count(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getPlateNumber, request.getPlateNumber())
        );
        if (count > 0) {
            throw new BusinessException("该车牌号已被注册");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(currentUserId);
        vehicle.setPlateNumber(request.getPlateNumber());

        if (StrUtil.isNotBlank(request.getVehicleType())) {
            vehicle.setVehicleType(request.getVehicleType());
        }
        if (StrUtil.isNotBlank(request.getBrand())) {
            vehicle.setBrand(request.getBrand());
        }
        if (StrUtil.isNotBlank(request.getColor())) {
            vehicle.setColor(request.getColor());
        }
        if (request.getIsDefault() != null) {
            vehicle.setIsDefault(request.getIsDefault());
        }

        save(vehicle);
        log.info("用户添加车辆, userId={}, plateNumber={}", currentUserId, request.getPlateNumber());
        return vehicle;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }

        // 检查权限：仅车辆所有者或管理员可操作
        checkOwnership(vehicle);

        // 如果修改了车牌号，检查新车牌号是否已被其他车辆使用
        if (StrUtil.isNotBlank(request.getPlateNumber())
                && !request.getPlateNumber().equals(vehicle.getPlateNumber())) {
            Long count = count(new LambdaQueryWrapper<Vehicle>()
                    .eq(Vehicle::getPlateNumber, request.getPlateNumber())
            );
            if (count > 0) {
                throw new BusinessException("该车牌号已被注册");
            }
            vehicle.setPlateNumber(request.getPlateNumber());
        }

        if (StrUtil.isNotBlank(request.getVehicleType())) {
            vehicle.setVehicleType(request.getVehicleType());
        }
        if (StrUtil.isNotBlank(request.getBrand())) {
            vehicle.setBrand(request.getBrand());
        }
        if (StrUtil.isNotBlank(request.getColor())) {
            vehicle.setColor(request.getColor());
        }
        if (request.getIsDefault() != null) {
            vehicle.setIsDefault(request.getIsDefault());
        }

        updateById(vehicle);
        log.info("更新车辆信息, vehicleId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }

        // 检查权限
        checkOwnership(vehicle);

        removeById(id);
        log.info("删除车辆, vehicleId={}", id);
    }

    /**
     * 检查当前用户是否为车辆所有者或管理员
     */
    private void checkOwnership(Vehicle vehicle) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentRole();

        if (!"ADMIN".equals(currentRole) && !vehicle.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作该车辆");
        }
    }
}
