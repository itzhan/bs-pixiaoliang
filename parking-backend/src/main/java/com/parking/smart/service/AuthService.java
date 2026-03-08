package com.parking.smart.service;

import com.parking.smart.dto.LoginRequest;
import com.parking.smart.dto.LoginResponse;
import com.parking.smart.dto.RegisterRequest;
import com.parking.smart.entity.User;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 退出登录（令牌加入黑名单）
     */
    void logout(String token);

    /**
     * 获取当前登录用户信息
     */
    User getCurrentUser();
}
