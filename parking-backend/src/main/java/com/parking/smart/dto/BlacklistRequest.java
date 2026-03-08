package com.parking.smart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlacklistRequest {

    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;

    @NotBlank(message = "拉黑原因不能为空")
    private String reason;
}
