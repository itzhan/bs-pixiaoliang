package com.parking.smart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SettingUpdateRequest {

    @NotNull(message = "配置项列表不能为空")
    @Valid
    private List<SettingItem> settings;

    @Data
    public static class SettingItem {

        @NotBlank(message = "配置键不能为空")
        private String key;

        private String value;
    }
}
