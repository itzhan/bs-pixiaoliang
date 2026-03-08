package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.VehicleRequest;
import com.parking.smart.entity.Vehicle;
import com.parking.smart.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * 查询我的车辆
     */
    @GetMapping("/my")
    public Result<List<Vehicle>> getMyVehicles() {
        List<Vehicle> vehicles = vehicleService.getMyVehicles();
        return Result.success(vehicles);
    }

    /**
     * 分页查询所有车辆（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Vehicle>> getVehicles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        PageResult<Vehicle> result = vehicleService.getVehicles(page, size, keyword, userId);
        return Result.success(result);
    }

    /**
     * 添加车辆
     */
    @PostMapping
    public Result<Vehicle> addVehicle(@Valid @RequestBody VehicleRequest request) {
        log.info("添加车辆: {}", request.getPlateNumber());
        Vehicle vehicle = vehicleService.addVehicle(request);
        return Result.success(vehicle);
    }

    /**
     * 更新车辆信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateVehicle(@PathVariable Long id,
                                      @Valid @RequestBody VehicleRequest request) {
        log.info("更新车辆信息: id={}", id);
        vehicleService.updateVehicle(id, request);
        return Result.success();
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteVehicle(@PathVariable Long id) {
        log.info("删除车辆: id={}", id);
        vehicleService.deleteVehicle(id);
        return Result.success();
    }
}
