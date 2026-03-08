package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.AnnouncementRequest;
import com.parking.smart.entity.Announcement;

import java.util.List;

public interface AnnouncementService extends IService<Announcement> {

    List<Announcement> getPublishedAnnouncements();

    PageResult<Announcement> getAnnouncements(Integer page, Integer size, Integer status, String keyword);

    Announcement createAnnouncement(AnnouncementRequest request);

    void updateAnnouncement(Long id, AnnouncementRequest request);

    void publishAnnouncement(Long id);

    void deleteAnnouncement(Long id);
}
