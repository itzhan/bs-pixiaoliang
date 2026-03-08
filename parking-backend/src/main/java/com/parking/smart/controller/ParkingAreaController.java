package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.ParkingAreaRequest;
import com.parking.smart.entity.ParkingArea;
import com.parking.smart.service.ParkingAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 停车区域控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/parking-areas")
@RequiredArgsConstructor
public class ParkingAreaController {

    private final ParkingAreaService parkingAreaService;

    /**
     * 查询所有启用的停车区域
     */
    @GetMapping
    public Result<List<ParkingArea>> getAllActiveAreas() {
        List<ParkingArea> areas = parkingAreaService.getAllActiveAreas();
        return Result.success(areas);
    }

    /**
     * 分页查询停车区域
     */
    @GetMapping("/page")
    public Result<PageResult<ParkingArea>> getAreas(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        PageResult<ParkingArea> result = parkingAreaService.getAreas(page, size, keyword);
        return Result.success(result);
    }

    /**
     * 查询停车区域统计信息
     */
    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getAreaStats() {
        List<Map<String, Object>> stats = parkingAreaService.getAreaStats();
        return Result.success(stats);
    }

    /**
     * 根据ID查询停车区域
     */
    @GetMapping("/{id}")
    public Result<ParkingArea> getAreaById(@PathVariable Long id) {
        ParkingArea area = parkingAreaService.getAreaById(id);
        return Result.success(area);
    }

    /**
     * 创建停车区域（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ParkingArea> createArea(@Valid @RequestBody ParkingAreaRequest request) {
        log.info("创建停车区域: {}", request.getName());
        ParkingArea area = parkingAreaService.createArea(request);
        return Result.success(area);
    }

    /**
     * 更新停车区域（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateArea(@PathVariable Long id,
                                   @Valid @RequestBody ParkingAreaRequest request) {
        log.info("更新停车区域: id={}", id);
        parkingAreaService.updateArea(id, request);
        return Result.success();
    }

    /**
     * 删除停车区域（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteArea(@PathVariable Long id) {
        log.info("删除停车区域: id={}", id);
        parkingAreaService.deleteArea(id);
        return Result.success();
    }
}
