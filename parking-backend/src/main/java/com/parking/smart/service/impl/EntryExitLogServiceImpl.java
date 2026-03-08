package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.PageResult;
import com.parking.smart.entity.EntryExitLog;
import com.parking.smart.mapper.EntryExitLogMapper;
import com.parking.smart.service.EntryExitLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntryExitLogServiceImpl extends ServiceImpl<EntryExitLogMapper, EntryExitLog> implements EntryExitLogService {

    @Override
    public PageResult<EntryExitLog> getLogs(Integer page, Integer size, String plateNumber, String logType) {
        LambdaQueryWrapper<EntryExitLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(plateNumber)) {
            wrapper.like(EntryExitLog::getPlateNumber, plateNumber);
        }
        if (StrUtil.isNotBlank(logType)) {
            wrapper.eq(EntryExitLog::getLogType, logType);
        }
        wrapper.orderByDesc(EntryExitLog::getCreatedAt);
        IPage<EntryExitLog> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    public void createLog(String plateNumber, String logType, String gateName, Long orderId) {
        EntryExitLog entryExitLog = new EntryExitLog();
        entryExitLog.setPlateNumber(plateNumber);
        entryExitLog.setLogType(logType);
        entryExitLog.setGateName(gateName);
        entryExitLog.setOrderId(orderId);
        save(entryExitLog);
        log.info("出入场记录创建成功, plateNumber={}, logType={}, gateName={}", plateNumber, logType, gateName);
    }
}
