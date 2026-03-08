package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.BillingRuleRequest;
import com.parking.smart.entity.BillingRule;
import com.parking.smart.service.BillingRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计费规则控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/billing-rules")
@RequiredArgsConstructor
public class BillingRuleController {

    private final BillingRuleService billingRuleService;

    /**
     * 查询所有计费规则
     */
    @GetMapping
    public Result<List<BillingRule>> getAllRules() {
        List<BillingRule> rules = billingRuleService.getAllRules();
        return Result.success(rules);
    }

    /**
     * 分页查询计费规则（管理员）
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<BillingRule>> getRules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<BillingRule> result = billingRuleService.getRules(page, size);
        return Result.success(result);
    }

    /**
     * 创建计费规则（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BillingRule> createRule(@Valid @RequestBody BillingRuleRequest request) {
        log.info("创建计费规则");
        BillingRule rule = billingRuleService.createRule(request);
        return Result.success(rule);
    }

    /**
     * 更新计费规则（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateRule(@PathVariable Long id,
                                   @Valid @RequestBody BillingRuleRequest request) {
        log.info("更新计费规则: id={}", id);
        billingRuleService.updateRule(id, request);
        return Result.success();
    }

    /**
     * 删除计费规则（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteRule(@PathVariable Long id) {
        log.info("删除计费规则: id={}", id);
        billingRuleService.deleteRule(id);
        return Result.success();
    }
}
