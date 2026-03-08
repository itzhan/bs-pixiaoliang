package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.AnnouncementRequest;
import com.parking.smart.entity.Announcement;
import com.parking.smart.mapper.AnnouncementMapper;
import com.parking.smart.service.AnnouncementService;
import com.parking.smart.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public List<Announcement> getPublishedAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getPublishedAt);
        return list(wrapper);
    }

    @Override
    public PageResult<Announcement> getAnnouncements(Integer page, Integer size, Integer status, String keyword) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Announcement::getTitle, keyword)
                    .or()
                    .like(Announcement::getContent, keyword));
        }
        wrapper.orderByDesc(Announcement::getCreatedAt);
        IPage<Announcement> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    public Announcement createAnnouncement(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        if (StrUtil.isNotBlank(request.getAnnouncementType())) {
            announcement.setAnnouncementType(request.getAnnouncementType());
        }
        if (request.getStatus() != null) {
            announcement.setStatus(request.getStatus());
        }
        announcement.setPublisherId(SecurityUtils.getCurrentUserId());
        save(announcement);
        log.info("公告创建成功, id={}, title={}", announcement.getId(), announcement.getTitle());
        return announcement;
    }

    @Override
    public void updateAnnouncement(Long id, AnnouncementRequest request) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        if (StrUtil.isNotBlank(request.getAnnouncementType())) {
            announcement.setAnnouncementType(request.getAnnouncementType());
        }
        if (request.getStatus() != null) {
            announcement.setStatus(request.getStatus());
        }
        updateById(announcement);
        log.info("公告更新成功, id={}", id);
    }

    @Override
    public void publishAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcement.setStatus(1);
        announcement.setPublishedAt(LocalDateTime.now());
        updateById(announcement);
        log.info("公告发布成功, id={}", id);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        removeById(id);
        log.info("公告删除成功, id={}", id);
    }
}
