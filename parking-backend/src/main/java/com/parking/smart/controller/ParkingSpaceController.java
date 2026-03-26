package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.ParkingSpaceRequest;
import com.parking.smart.entity.ParkingSpace;
import com.parking.smart.service.ParkingSpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 停车位控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/parking-spaces")
@RequiredArgsConstructor
public class ParkingSpaceController {

    private final ParkingSpaceService parkingSpaceService;

    /**
     * 查询可用停车位
     */
    @GetMapping("/available")
    public Result<List<ParkingSpace>> getAvailableSpaces(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String spaceType) {
        List<ParkingSpace> spaces = parkingSpaceService.getAvailableSpaces(areaId, spaceType);
        return Result.success(spaces);
    }

    /**
     * 分页查询停车位
     */
    @GetMapping
    public Result<PageResult<ParkingSpace>> getSpaces(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String spaceType) {
        PageResult<ParkingSpace> result = parkingSpaceService.getSpaces(page, size, areaId, status, spaceType);
        return Result.success(result);
    }

    /**
     * 创建停车位（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ParkingSpace> createSpace(@Valid @RequestBody ParkingSpaceRequest request) {
        log.info("创建停车位: {}", request.getSpaceNumber());
        ParkingSpace space = parkingSpaceService.createSpace(request);
        return Result.success(space);
    }

    /**
     * 更新停车位（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateSpace(@PathVariable Long id,
                                    @Valid @RequestBody ParkingSpaceRequest request) {
        log.info("更新停车位: id={}", id);
        parkingSpaceService.updateSpace(id, request);
        return Result.success();
    }

    /**
     * 更新停车位状态（管理员）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<Void> updateSpaceStatus(@PathVariable Long id,
                                          @RequestParam Integer status) {
        log.info("更新停车位状态: id={}, status={}", id, status);
        parkingSpaceService.updateSpaceStatus(id, status);
        return Result.success();
    }

    /**
     * 删除停车位（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteSpace(@PathVariable Long id) {
        log.info("删除停车位: id={}", id);
        parkingSpaceService.deleteSpace(id);
        return Result.success();
    }
}
