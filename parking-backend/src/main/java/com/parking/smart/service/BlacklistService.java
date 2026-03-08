package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.BlacklistRequest;
import com.parking.smart.entity.Blacklist;

public interface BlacklistService extends IService<Blacklist> {

    /**
     * 分页查询黑名单
     */
    PageResult<Blacklist> getBlacklist(Integer page, Integer size, Integer status, String keyword);

    /**
     * 添加黑名单
     */
    Blacklist addToBlacklist(BlacklistRequest request);

    /**
     * 移除黑名单
     */
    void removeFromBlacklist(Long id);

    /**
     * 判断车牌是否在黑名单中
     */
    boolean isBlacklisted(String plateNumber);
}
