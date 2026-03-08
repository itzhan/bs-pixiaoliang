package com.parking.smart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.smart.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
