package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.BlacklistRequest;
import com.parking.smart.entity.Blacklist;
import com.parking.smart.mapper.BlacklistMapper;
import com.parking.smart.service.BlacklistService;
import com.parking.smart.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    @Override
    public PageResult<Blacklist> getBlacklist(Integer page, Integer size, Integer status, String keyword) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<Blacklist>()
                .eq(status != null, Blacklist::getStatus, status)
                .like(StrUtil.isNotBlank(keyword), Blacklist::getPlateNumber, keyword)
                .orderByDesc(Blacklist::getCreatedAt);

        IPage<Blacklist> iPage = baseMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Blacklist addToBlacklist(BlacklistRequest request) {
        // 检查是否已在黑名单中
        Long existCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getPlateNumber, request.getPlateNumber())
                        .eq(Blacklist::getStatus, 1)
        );
        if (existCount > 0) {
            throw new BusinessException("该车牌已在黑名单中");
        }

        Blacklist blacklist = new Blacklist();
        blacklist.setPlateNumber(request.getPlateNumber());
        blacklist.setReason(request.getReason());
        blacklist.setStatus(1);
        blacklist.setBlockedAt(LocalDateTime.now());
        blacklist.setOperatorId(SecurityUtils.getCurrentUserId());
        baseMapper.insert(blacklist);

        log.info("车牌 {} 已加入黑名单，原因：{}", request.getPlateNumber(), request.getReason());
        return blacklist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromBlacklist(Long id) {
        Blacklist blacklist = baseMapper.selectById(id);
        if (blacklist == null) {
            throw new BusinessException("黑名单记录不存在");
        }
        if (blacklist.getStatus() == 0) {
            throw new BusinessException("该记录已解除");
        }

        blacklist.setStatus(0);
        blacklist.setRemovedAt(LocalDateTime.now());
        baseMapper.updateById(blacklist);

        log.info("车牌 {} 已从黑名单移除", blacklist.getPlateNumber());
    }

    @Override
    public boolean isBlacklisted(String plateNumber) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getPlateNumber, plateNumber)
                        .eq(Blacklist::getStatus, 1)
        );
        return count > 0;
    }
}
