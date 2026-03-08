package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.PageResult;
import com.parking.smart.entity.AuditLog;
import com.parking.smart.mapper.AuditLogMapper;
import com.parking.smart.service.AuditLogService;
import com.parking.smart.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    private final HttpServletRequest httpServletRequest;

    @Override
    public PageResult<AuditLog> getLogs(Integer page, Integer size, String module, String action) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(module)) {
            wrapper.eq(AuditLog::getModule, module);
        }
        if (StrUtil.isNotBlank(action)) {
            wrapper.eq(AuditLog::getAction, action);
        }
        wrapper.orderByDesc(AuditLog::getCreatedAt);
        IPage<AuditLog> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    public void log(String module, String action, String detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(SecurityUtils.getCurrentUserId());
        auditLog.setUsername(SecurityUtils.getCurrentUsername());
        auditLog.setModule(module);
        auditLog.setAction(action);
        auditLog.setDetail(detail);
        auditLog.setIp(getClientIp());
        save(auditLog);
        log.info("审计日志记录, module={}, action={}, userId={}", module, action, auditLog.getUserId());
    }

    private String getClientIp() {
        String ip = httpServletRequest.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getRemoteAddr();
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
