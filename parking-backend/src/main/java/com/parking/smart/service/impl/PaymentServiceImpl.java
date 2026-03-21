package com.parking.smart.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.PaymentRequest;
import com.parking.smart.entity.Message;
import com.parking.smart.entity.ParkingOrder;
import com.parking.smart.entity.Payment;
import com.parking.smart.mapper.MessageMapper;
import com.parking.smart.mapper.ParkingOrderMapper;
import com.parking.smart.mapper.PaymentMapper;
import com.parking.smart.service.PaymentService;
import com.parking.smart.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private ParkingOrderMapper parkingOrderMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(PaymentRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 查找订单
        ParkingOrder order = parkingOrderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getPaymentStatus() != 0) {
            throw new BusinessException("该订单已支付或已退款");
        }

        // 生成支付编号
        String paymentNo = "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + RandomUtil.randomNumbers(4);

        // 创建支付记录
        Payment payment = new Payment();
        payment.setPaymentNo(paymentNo);
        payment.setOrderId(order.getId());
        payment.setUserId(userId);
        payment.setAmount(order.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(1); // 已支付
        payment.setPaidAt(LocalDateTime.now());
        baseMapper.insert(payment);

        // 更新订单支付状态
        order.setPaymentStatus(1); // 已支付
        parkingOrderMapper.updateById(order);

        // 创建消息通知
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle("支付成功");
        message.setContent("支付成功，金额: ¥" + order.getAmount());
        message.setMessageType("PAYMENT");
        messageMapper.insert(message);

        log.info("用户 {} 支付成功，支付编号：{}，金额：{}", userId, paymentNo, order.getAmount());
        return payment;
    }

    @Override
    public PageResult<Payment> getMyPayments(Integer page, Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<Payment>()
                .eq(Payment::getUserId, userId)
                .orderByDesc(Payment::getCreatedAt);

        IPage<Payment> iPage = baseMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Autowired
    private com.parking.smart.mapper.UserMapper userMapper;

    @Override
    public PageResult<Payment> getAllPayments(Integer page, Integer size, Integer status, String keyword) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<Payment>()
                .eq(status != null, Payment::getStatus, status)
                .like(StrUtil.isNotBlank(keyword), Payment::getPaymentNo, keyword)
                .orderByDesc(Payment::getCreatedAt);

        IPage<Payment> iPage = baseMapper.selectPage(new Page<>(page, size), wrapper);
        fillPaymentNames(iPage.getRecords());
        return PageResult.from(iPage);
    }

    private void fillPaymentNames(java.util.List<Payment> list) {
        if (list == null || list.isEmpty()) return;
        // 订单号
        java.util.Set<Long> orderIds = list.stream().map(Payment::getOrderId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, String> orderNoMap = new java.util.HashMap<>();
        if (!orderIds.isEmpty()) {
            parkingOrderMapper.selectBatchIds(orderIds).forEach(o -> orderNoMap.put(o.getId(), o.getOrderNo()));
        }
        // 用户名
        java.util.Set<Long> userIds = list.stream().map(Payment::getUserId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, String> userNameMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userNameMap.put(u.getId(), u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }
        for (Payment p : list) {
            p.setOrderNo(orderNoMap.get(p.getOrderId()));
            p.setUserName(userNameMap.get(p.getUserId()));
        }
    }
}
