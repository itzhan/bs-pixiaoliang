package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ParkingAreaRequest;
import com.parking.smart.entity.ParkingArea;
import com.parking.smart.mapper.ParkingAreaMapper;
import com.parking.smart.service.ParkingAreaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class ParkingAreaServiceImpl extends ServiceImpl<ParkingAreaMapper, ParkingArea> implements ParkingAreaService {

    @Override
    public List<ParkingArea> getAllActiveAreas() {
        return list(new LambdaQueryWrapper<ParkingArea>()
                .eq(ParkingArea::getStatus, 1)
                .orderByAsc(ParkingArea::getFloorNumber)
                .orderByAsc(ParkingArea::getCode)
        );
    }

    @Override
    public PageResult<ParkingArea> getAreas(Integer page, Integer size, String keyword) {
        Page<ParkingArea> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<ParkingArea> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索：区域名称、区域编码
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(ParkingArea::getName, keyword)
                    .or().like(ParkingArea::getCode, keyword)
            );
        }

        wrapper.orderByDesc(ParkingArea::getCreatedAt);

        Page<ParkingArea> result = page(pageParam, wrapper);
        return PageResult.from(result);
    }

    @Override
    public List<Map<String, Object>> getAreaStats() {
        List<ParkingArea> areas = list(new LambdaQueryWrapper<ParkingArea>()
                .eq(ParkingArea::getStatus, 1)
        );

        List<Map<String, Object>> stats = new ArrayList<>();
        for (ParkingArea area : areas) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("id", area.getId());
            stat.put("name", area.getName());

            int totalSpaces = area.getTotalSpaces() != null ? area.getTotalSpaces() : 0;
            int availableSpaces = area.getAvailableSpaces() != null ? area.getAvailableSpaces() : 0;
            int occupiedSpaces = totalSpaces - availableSpaces;

            stat.put("totalSpaces", totalSpaces);
            stat.put("availableSpaces", availableSpaces);
            stat.put("occupiedSpaces", occupiedSpaces);

            // 计算占用率
            BigDecimal occupancyRate = BigDecimal.ZERO;
            if (totalSpaces > 0) {
                occupancyRate = BigDecimal.valueOf(occupiedSpaces)
                        .divide(BigDecimal.valueOf(totalSpaces), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            stat.put("occupancyRate", occupancyRate);

            stats.add(stat);
        }
        return stats;
    }

    @Override
    public ParkingArea getAreaById(Long id) {
        ParkingArea area = getById(id);
        if (area == null) {
            throw new BusinessException("停车区域不存在");
        }
        return area;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingArea createArea(ParkingAreaRequest request) {
        // 检查编码唯一性
        Long count = count(new LambdaQueryWrapper<ParkingArea>()
                .eq(ParkingArea::getCode, request.getCode())
        );
        if (count > 0) {
            throw new BusinessException("区域编码已存在");
        }

        ParkingArea area = new ParkingArea();
        area.setName(request.getName());
        area.setCode(request.getCode());
        area.setDescription(request.getDescription());
        area.setTotalSpaces(request.getTotalSpaces());
        // 新建区域时可用车位数等于总车位数
        area.setAvailableSpaces(request.getTotalSpaces());

        if (StrUtil.isNotBlank(request.getFloorNumber())) {
            area.setFloorNumber(request.getFloorNumber());
        }
        if (StrUtil.isNotBlank(request.getAreaType())) {
            area.setAreaType(request.getAreaType());
        }
        if (request.getStatus() != null) {
            area.setStatus(request.getStatus());
        }

        save(area);
        log.info("创建停车区域: {}", area.getName());
        return area;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArea(Long id, ParkingAreaRequest request) {
        ParkingArea area = getById(id);
        if (area == null) {
            throw new BusinessException("停车区域不存在");
        }

        if (StrUtil.isNotBlank(request.getName())) {
            area.setName(request.getName());
        }
        // 如果修改编码，检查唯一性
        if (StrUtil.isNotBlank(request.getCode()) && !request.getCode().equals(area.getCode())) {
            Long count = count(new LambdaQueryWrapper<ParkingArea>()
                    .eq(ParkingArea::getCode, request.getCode())
            );
            if (count > 0) {
                throw new BusinessException("区域编码已存在");
            }
            area.setCode(request.getCode());
        }
        if (request.getDescription() != null) {
            area.setDescription(request.getDescription());
        }
        if (request.getTotalSpaces() != null) {
            // 更新总车位数时，同步调整可用车位数
            int diff = request.getTotalSpaces() - area.getTotalSpaces();
            area.setTotalSpaces(request.getTotalSpaces());
            int newAvailable = (area.getAvailableSpaces() != null ? area.getAvailableSpaces() : 0) + diff;
            area.setAvailableSpaces(Math.max(newAvailable, 0));
        }
        if (StrUtil.isNotBlank(request.getFloorNumber())) {
            area.setFloorNumber(request.getFloorNumber());
        }
        if (StrUtil.isNotBlank(request.getAreaType())) {
            area.setAreaType(request.getAreaType());
        }
        if (request.getStatus() != null) {
            area.setStatus(request.getStatus());
        }

        updateById(area);
        log.info("更新停车区域, areaId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArea(Long id) {
        ParkingArea area = getById(id);
        if (area == null) {
            throw new BusinessException("停车区域不存在");
        }
        removeById(id);
        log.info("删除停车区域, areaId={}", id);
    }
}
