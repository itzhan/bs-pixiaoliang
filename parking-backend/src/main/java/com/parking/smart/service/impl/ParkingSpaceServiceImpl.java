package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ParkingSpaceRequest;
import com.parking.smart.entity.ParkingArea;
import com.parking.smart.entity.ParkingSpace;
import com.parking.smart.mapper.ParkingSpaceMapper;
import com.parking.smart.service.ParkingAreaService;
import com.parking.smart.service.ParkingSpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingSpaceServiceImpl extends ServiceImpl<ParkingSpaceMapper, ParkingSpace> implements ParkingSpaceService {

    private final ParkingAreaService parkingAreaService;

    @Override
    public List<ParkingSpace> getAvailableSpaces(Long areaId, String spaceType) {
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        // 状态0=空闲
        wrapper.eq(ParkingSpace::getStatus, 0);

        if (areaId != null) {
            wrapper.eq(ParkingSpace::getAreaId, areaId);
        }
        if (StrUtil.isNotBlank(spaceType)) {
            wrapper.eq(ParkingSpace::getSpaceType, spaceType);
        }

        wrapper.orderByAsc(ParkingSpace::getSpaceNumber);
        List<ParkingSpace> spaces = list(wrapper);
        // 填充区域名称
        fillAreaName(spaces);
        return spaces;
    }

    private void fillAreaName(List<ParkingSpace> spaces) {
        if (spaces == null || spaces.isEmpty()) return;
        java.util.Set<Long> areaIds = spaces.stream().map(ParkingSpace::getAreaId)
                .filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        if (areaIds.isEmpty()) return;
        java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
        parkingAreaService.listByIds(areaIds).forEach(a -> nameMap.put(a.getId(), a.getName()));
        spaces.forEach(s -> s.setAreaName(nameMap.get(s.getAreaId())));
    }

    @Override
    public PageResult<ParkingSpace> getSpaces(Integer page, Integer size, Long areaId, Integer status, String spaceType) {
        Page<ParkingSpace> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();

        if (areaId != null) {
            wrapper.eq(ParkingSpace::getAreaId, areaId);
        }
        if (status != null) {
            wrapper.eq(ParkingSpace::getStatus, status);
        }
        if (StrUtil.isNotBlank(spaceType)) {
            wrapper.eq(ParkingSpace::getSpaceType, spaceType);
        }

        wrapper.orderByAsc(ParkingSpace::getSpaceNumber);

        Page<ParkingSpace> result = page(pageParam, wrapper);
        return PageResult.from(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingSpace createSpace(ParkingSpaceRequest request) {
        // 检查区域是否存在
        ParkingArea area = parkingAreaService.getAreaById(request.getAreaId());

        // 检查车位编号在同一区域内是否唯一
        Long count = count(new LambdaQueryWrapper<ParkingSpace>()
                .eq(ParkingSpace::getAreaId, request.getAreaId())
                .eq(ParkingSpace::getSpaceNumber, request.getSpaceNumber())
        );
        if (count > 0) {
            throw new BusinessException("该区域下车位编号已存在");
        }

        ParkingSpace space = new ParkingSpace();
        space.setAreaId(request.getAreaId());
        space.setSpaceNumber(request.getSpaceNumber());

        if (StrUtil.isNotBlank(request.getSpaceType())) {
            space.setSpaceType(request.getSpaceType());
        }
        if (request.getStatus() != null) {
            space.setStatus(request.getStatus());
        }
        if (request.getHourlyRate() != null) {
            space.setHourlyRate(request.getHourlyRate());
        }

        save(space);

        // 更新区域的总车位数和可用车位数
        area.setTotalSpaces((area.getTotalSpaces() != null ? area.getTotalSpaces() : 0) + 1);
        // 新车位默认空闲(status=0)则可用车位数+1
        if (space.getStatus() == null || space.getStatus() == 0) {
            area.setAvailableSpaces((area.getAvailableSpaces() != null ? area.getAvailableSpaces() : 0) + 1);
        }
        parkingAreaService.updateById(area);

        log.info("创建车位, areaId={}, spaceNumber={}", request.getAreaId(), request.getSpaceNumber());
        return space;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpace(Long id, ParkingSpaceRequest request) {
        ParkingSpace space = getById(id);
        if (space == null) {
            throw new BusinessException("车位不存在");
        }

        // 如果修改了区域，检查新区域是否存在
        if (request.getAreaId() != null && !request.getAreaId().equals(space.getAreaId())) {
            parkingAreaService.getAreaById(request.getAreaId());
            space.setAreaId(request.getAreaId());
        }

        // 如果修改了车位编号，检查唯一性
        if (StrUtil.isNotBlank(request.getSpaceNumber())
                && !request.getSpaceNumber().equals(space.getSpaceNumber())) {
            Long count = count(new LambdaQueryWrapper<ParkingSpace>()
                    .eq(ParkingSpace::getAreaId, space.getAreaId())
                    .eq(ParkingSpace::getSpaceNumber, request.getSpaceNumber())
            );
            if (count > 0) {
                throw new BusinessException("该区域下车位编号已存在");
            }
            space.setSpaceNumber(request.getSpaceNumber());
        }

        if (StrUtil.isNotBlank(request.getSpaceType())) {
            space.setSpaceType(request.getSpaceType());
        }
        if (request.getHourlyRate() != null) {
            space.setHourlyRate(request.getHourlyRate());
        }

        updateById(space);
        log.info("更新车位信息, spaceId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpaceStatus(Long id, Integer status) {
        ParkingSpace space = getById(id);
        if (space == null) {
            throw new BusinessException("车位不存在");
        }

        Integer oldStatus = space.getStatus();
        if (oldStatus.equals(status)) {
            return; // 状态没有变化，无需更新
        }

        space.setStatus(status);
        updateById(space);

        // 更新区域可用车位数
        ParkingArea area = parkingAreaService.getAreaById(space.getAreaId());
        int availableSpaces = area.getAvailableSpaces() != null ? area.getAvailableSpaces() : 0;

        // 从空闲变为其他状态：可用车位数-1
        if (oldStatus == 0 && status != 0) {
            area.setAvailableSpaces(Math.max(availableSpaces - 1, 0));
        }
        // 从其他状态变为空闲：可用车位数+1
        else if (oldStatus != 0 && status == 0) {
            area.setAvailableSpaces(availableSpaces + 1);
        }

        parkingAreaService.updateById(area);
        log.info("更新车位状态, spaceId={}, oldStatus={}, newStatus={}", id, oldStatus, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpace(Long id) {
        ParkingSpace space = getById(id);
        if (space == null) {
            throw new BusinessException("车位不存在");
        }

        removeById(id);

        // 更新区域的总车位数和可用车位数
        ParkingArea area = parkingAreaService.getAreaById(space.getAreaId());
        area.setTotalSpaces(Math.max((area.getTotalSpaces() != null ? area.getTotalSpaces() : 0) - 1, 0));

        // 如果删除的是空闲车位，可用车位数也要减少
        if (space.getStatus() != null && space.getStatus() == 0) {
            area.setAvailableSpaces(Math.max((area.getAvailableSpaces() != null ? area.getAvailableSpaces() : 0) - 1, 0));
        }

        parkingAreaService.updateById(area);
        log.info("删除车位, spaceId={}, areaId={}", id, space.getAreaId());
    }
}
