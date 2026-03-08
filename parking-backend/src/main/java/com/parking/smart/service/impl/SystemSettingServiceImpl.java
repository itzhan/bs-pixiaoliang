package com.parking.smart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.dto.SettingUpdateRequest;
import com.parking.smart.entity.SystemSetting;
import com.parking.smart.mapper.SystemSettingMapper;
import com.parking.smart.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl extends ServiceImpl<SystemSettingMapper, SystemSetting> implements SystemSettingService {

    @Override
    public List<SystemSetting> getAllSettings() {
        return list();
    }

    @Override
    public String getSettingValue(String key) {
        LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSetting::getSettingKey, key);
        SystemSetting setting = getOne(wrapper);
        return setting != null ? setting.getSettingValue() : null;
    }

    @Override
    public void updateSettings(List<SettingUpdateRequest.SettingItem> settings) {
        for (SettingUpdateRequest.SettingItem item : settings) {
            LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemSetting::getSettingKey, item.getKey());
            SystemSetting existing = getOne(wrapper);
            if (existing != null) {
                existing.setSettingValue(item.getValue());
                updateById(existing);
            } else {
                SystemSetting newSetting = new SystemSetting();
                newSetting.setSettingKey(item.getKey());
                newSetting.setSettingValue(item.getValue());
                save(newSetting);
            }
            log.info("系统设置更新, key={}, value={}", item.getKey(), item.getValue());
        }
    }
}
