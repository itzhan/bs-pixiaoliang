package com.parking.smart.controller;

import com.parking.smart.common.Result;
import com.parking.smart.dto.LoginRequest;
import com.parking.smart.dto.LoginResponse;
import com.parking.smart.dto.RegisterRequest;
import com.parking.smart.entity.User;
import com.parking.smart.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册: {}", request.getUsername());
        LoginResponse response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("用户退出登录");
        authService.logout(token);
        return Result.success();
    }
}
