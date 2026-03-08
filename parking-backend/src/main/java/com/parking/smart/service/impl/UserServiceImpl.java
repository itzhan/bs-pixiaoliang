package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.PasswordUpdateRequest;
import com.parking.smart.dto.UserUpdateRequest;
import com.parking.smart.entity.User;
import com.parking.smart.mapper.UserMapper;
import com.parking.smart.service.UserService;
import com.parking.smart.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<User> getUsers(Integer page, Integer size, String keyword, String role) {
        Page<User> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索：用户名、真实姓名、手机号
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword)
            );
        }

        // 角色筛选
        if (StrUtil.isNotBlank(role)) {
            wrapper.eq(User::getRole, role);
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> result = page(pageParam, wrapper);

        // 隐藏密码
        result.getRecords().forEach(user -> user.setPassword(null));

        return PageResult.from(result);
    }

    @Override
    public User getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (StrUtil.isNotBlank(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StrUtil.isNotBlank(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StrUtil.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StrUtil.isNotBlank(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (StrUtil.isNotBlank(request.getRole())) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        updateById(user);
        log.info("管理员更新用户信息, userId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        User user = getById(currentUserId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (StrUtil.isNotBlank(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StrUtil.isNotBlank(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StrUtil.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StrUtil.isNotBlank(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }

        updateById(user);
        log.info("用户更新个人资料, userId={}", currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        User user = getById(currentUserId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);
        log.info("用户修改密码成功, userId={}", currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        removeById(id);
        log.info("删除用户, userId={}", id);
    }
}
