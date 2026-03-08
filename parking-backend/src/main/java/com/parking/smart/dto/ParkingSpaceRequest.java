package com.parking.smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParkingSpaceRequest {

    @NotNull(message = "所属区域不能为空")
    private Long areaId;

    @NotBlank(message = "车位编号不能为空")
    private String spaceNumber;

    private String spaceType;

    private Integer status;

    private BigDecimal hourlyRate;
}
