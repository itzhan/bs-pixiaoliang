package com.parking.smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillingRuleRequest {

    @NotBlank(message = "规则名称不能为空")
    private String name;

    @NotBlank(message = "车位类型不能为空")
    private String spaceType;

    @NotNull(message = "每小时费率不能为空")
    private BigDecimal hourlyRate;

    private BigDecimal dailyCap;

    private Integer freeMinutes;

    private Integer isActive;

    private String description;
}
