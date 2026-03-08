package com.parking.smart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.smart.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
