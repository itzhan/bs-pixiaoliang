package com.parking.smart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParkingAreaRequest {

    @NotBlank(message = "区域名称不能为空")
    private String name;

    @NotBlank(message = "区域编码不能为空")
    private String code;

    private String description;

    @NotNull(message = "总车位数不能为空")
    @Min(value = 0, message = "总车位数不能小于0")
    private Integer totalSpaces;

    private String floorNumber;

    private String areaType;

    private Integer status;
}
