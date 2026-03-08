package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.entity.AuditLog;

public interface AuditLogService extends IService<AuditLog> {

    PageResult<AuditLog> getLogs(Integer page, Integer size, String module, String action);

    void log(String module, String action, String detail);
}
