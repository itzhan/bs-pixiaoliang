package com.parking.smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderEntryRequest {

    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;

    @NotNull(message = "车位不能为空")
    private Long spaceId;
}
