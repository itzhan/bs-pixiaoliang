package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.dto.SettingUpdateRequest;
import com.parking.smart.entity.SystemSetting;

import java.util.List;

public interface SystemSettingService extends IService<SystemSetting> {

    List<SystemSetting> getAllSettings();

    String getSettingValue(String key);

    void updateSettings(List<SettingUpdateRequest.SettingItem> settings);
}
