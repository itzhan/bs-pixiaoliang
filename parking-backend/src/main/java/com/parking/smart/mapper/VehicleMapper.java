package com.parking.smart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.smart.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
}
