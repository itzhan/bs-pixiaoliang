package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.VehicleRequest;
import com.parking.smart.entity.Vehicle;

import java.util.List;

/**
 * 车辆服务
 */
public interface VehicleService extends IService<Vehicle> {

    /**
     * 获取当前用户的车辆列表
     */
    List<Vehicle> getMyVehicles();

    /**
     * 分页查询车辆列表（管理员）
     */
    PageResult<Vehicle> getVehicles(Integer page, Integer size, String keyword, Long userId);

    /**
     * 添加车辆
     */
    Vehicle addVehicle(VehicleRequest request);

    /**
     * 更新车辆信息
     */
    void updateVehicle(Long id, VehicleRequest request);

    /**
     * 删除车辆
     */
    void deleteVehicle(Long id);
}
