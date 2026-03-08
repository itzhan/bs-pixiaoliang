package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ParkingSpaceRequest;
import com.parking.smart.entity.ParkingSpace;

import java.util.List;

/**
 * 停车位服务
 */
public interface ParkingSpaceService extends IService<ParkingSpace> {

    /**
     * 获取可用车位列表
     */
    List<ParkingSpace> getAvailableSpaces(Long areaId, String spaceType);

    /**
     * 分页查询车位列表
     */
    PageResult<ParkingSpace> getSpaces(Integer page, Integer size, Long areaId, Integer status, String spaceType);

    /**
     * 创建车位
     */
    ParkingSpace createSpace(ParkingSpaceRequest request);

    /**
     * 更新车位信息
     */
    void updateSpace(Long id, ParkingSpaceRequest request);

    /**
     * 更新车位状态
     */
    void updateSpaceStatus(Long id, Integer status);

    /**
     * 删除车位
     */
    void deleteSpace(Long id);
}
