package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.entity.EntryExitLog;

public interface EntryExitLogService extends IService<EntryExitLog> {

    PageResult<EntryExitLog> getLogs(Integer page, Integer size, String plateNumber, String logType);

    void createLog(String plateNumber, String logType, String gateName, Long orderId);
}
