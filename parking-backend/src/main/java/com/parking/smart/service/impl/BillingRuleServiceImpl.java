package com.parking.smart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.BillingRuleRequest;
import com.parking.smart.entity.BillingRule;
import com.parking.smart.mapper.BillingRuleMapper;
import com.parking.smart.service.BillingRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class BillingRuleServiceImpl extends ServiceImpl<BillingRuleMapper, BillingRule> implements BillingRuleService {

    @Override
    public List<BillingRule> getAllRules() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<BillingRule>()
                        .orderByDesc(BillingRule::getCreatedAt)
        );
    }

    @Override
    public PageResult<BillingRule> getRules(Integer page, Integer size) {
        IPage<BillingRule> iPage = baseMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<BillingRule>()
                        .orderByDesc(BillingRule::getCreatedAt)
        );
        return PageResult.from(iPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingRule createRule(BillingRuleRequest request) {
        // 检查同类型车位是否已有启用的规则
        Long existCount = baseMapper.selectCount(
                new LambdaQueryWrapper<BillingRule>()
                        .eq(BillingRule::getSpaceType, request.getSpaceType())
                        .eq(BillingRule::getIsActive, 1)
        );
        if (existCount > 0) {
            throw new BusinessException("该车位类型已存在启用的计费规则");
        }

        BillingRule rule = new BillingRule();
        rule.setName(request.getName());
        rule.setSpaceType(request.getSpaceType());
        rule.setHourlyRate(request.getHourlyRate());
        rule.setDailyCap(request.getDailyCap());
        rule.setFreeMinutes(request.getFreeMinutes() != null ? request.getFreeMinutes() : 15);
        rule.setIsActive(request.getIsActive() != null ? request.getIsActive() : 1);
        rule.setDescription(request.getDescription());
        baseMapper.insert(rule);

        log.info("创建计费规则：{}", rule.getName());
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(Long id, BillingRuleRequest request) {
        BillingRule rule = baseMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException("计费规则不存在");
        }

        // 如果修改了车位类型且启用，检查是否冲突
        if (!rule.getSpaceType().equals(request.getSpaceType())
                || (request.getIsActive() != null && request.getIsActive() == 1 && rule.getIsActive() != 1)) {
            Long existCount = baseMapper.selectCount(
                    new LambdaQueryWrapper<BillingRule>()
                            .eq(BillingRule::getSpaceType, request.getSpaceType())
                            .eq(BillingRule::getIsActive, 1)
                            .ne(BillingRule::getId, id)
            );
            if (existCount > 0) {
                throw new BusinessException("该车位类型已存在启用的计费规则");
            }
        }

        rule.setName(request.getName());
        rule.setSpaceType(request.getSpaceType());
        rule.setHourlyRate(request.getHourlyRate());
        if (request.getDailyCap() != null) {
            rule.setDailyCap(request.getDailyCap());
        }
        if (request.getFreeMinutes() != null) {
            rule.setFreeMinutes(request.getFreeMinutes());
        }
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        baseMapper.updateById(rule);

        log.info("更新计费规则：{}", rule.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        BillingRule rule = baseMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException("计费规则不存在");
        }
        baseMapper.deleteById(id);
        log.info("删除计费规则：{}", rule.getName());
    }
}
