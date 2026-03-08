package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.PasswordUpdateRequest;
import com.parking.smart.dto.UserUpdateRequest;
import com.parking.smart.entity.User;
import com.parking.smart.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        PageResult<User> result = userService.getUsers(page, size, keyword, role);
        return Result.success(result);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 更新用户信息（管理员）
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UserUpdateRequest request) {
        log.info("更新用户信息: id={}", id);
        userService.updateUser(id, request);
        return Result.success();
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        log.info("更新个人资料");
        userService.updateProfile(request);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        log.info("修改密码");
        userService.updatePassword(request);
        return Result.success();
    }

    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户: id={}", id);
        userService.deleteUser(id);
        return Result.success();
    }
}
