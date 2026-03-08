package com.parking.smart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.smart.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}
