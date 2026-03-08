package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ParkingAreaRequest;
import com.parking.smart.entity.ParkingArea;

import java.util.List;
import java.util.Map;

/**
 * 停车区域服务
 */
public interface ParkingAreaService extends IService<ParkingArea> {

    /**
     * 获取所有启用的停车区域
     */
    List<ParkingArea> getAllActiveAreas();

    /**
     * 分页查询停车区域
     */
    PageResult<ParkingArea> getAreas(Integer page, Integer size, String keyword);

    /**
     * 获取各区域统计信息
     */
    List<Map<String, Object>> getAreaStats();

    /**
     * 根据ID获取停车区域
     */
    ParkingArea getAreaById(Long id);

    /**
     * 创建停车区域
     */
    ParkingArea createArea(ParkingAreaRequest request);

    /**
     * 更新停车区域
     */
    void updateArea(Long id, ParkingAreaRequest request);

    /**
     * 删除停车区域
     */
    void deleteArea(Long id);
}
