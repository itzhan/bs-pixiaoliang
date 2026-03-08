package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.BillingRuleRequest;
import com.parking.smart.entity.BillingRule;

import java.util.List;

public interface BillingRuleService extends IService<BillingRule> {

    /**
     * 查询所有计费规则
     */
    List<BillingRule> getAllRules();

    /**
     * 分页查询计费规则
     */
    PageResult<BillingRule> getRules(Integer page, Integer size);

    /**
     * 创建计费规则
     */
    BillingRule createRule(BillingRuleRequest request);

    /**
     * 更新计费规则
     */
    void updateRule(Long id, BillingRuleRequest request);

    /**
     * 删除计费规则
     */
    void deleteRule(Long id);
}
