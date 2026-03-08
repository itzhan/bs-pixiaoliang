package com.parking.smart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleRequest {

    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;

    private String vehicleType;

    private String brand;

    private String color;

    private Integer isDefault;
}
