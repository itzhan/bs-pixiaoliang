package com.parking.smart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderExitRequest {

    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;
}
