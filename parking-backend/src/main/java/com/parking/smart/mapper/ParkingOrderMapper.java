package com.parking.smart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.smart.entity.ParkingOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingOrderMapper extends BaseMapper<ParkingOrder> {
}
