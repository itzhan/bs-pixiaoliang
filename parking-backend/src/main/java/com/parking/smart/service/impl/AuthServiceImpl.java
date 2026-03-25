package com.parking.smart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.smart.common.BusinessException;
import com.parking.smart.dto.LoginRequest;
import com.parking.smart.dto.LoginResponse;
import com.parking.smart.dto.RegisterRequest;
import com.parking.smart.entity.User;
import com.parking.smart.mapper.UserMapper;
import com.parking.smart.security.JwtUtils;
import com.parking.smart.service.AuthService;
import com.parking.smart.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    public LoginResponse login(LoginRequest request) {
        // 去除用户名前后空格
        String username = request.getUsername().trim();

        // 根据用户名查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        return buildLoginResponse(user, token);
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus(1);

        if (StrUtil.isNotBlank(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StrUtil.isNotBlank(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StrUtil.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }

        userMapper.insert(user);
        log.info("新用户注册成功: {}", user.getUsername());

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        return buildLoginResponse(user, token);
    }

    @Override
    public void logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            // 将token加入Redis黑名单，过期时间与JWT过期时间一致
            String key = TOKEN_BLACKLIST_PREFIX + token;
            // JWT默认过期时间（秒），使用相同的过期时间
            redisTemplate.opsForValue().set(key, "1", 24, TimeUnit.HOURS);
            log.info("用户退出登录，token已加入黑名单");
        }
    }

    @Override
    public User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return user;
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(User user, String token) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        return response;
    }
}
