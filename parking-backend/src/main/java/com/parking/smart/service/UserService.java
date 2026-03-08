package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.PasswordUpdateRequest;
import com.parking.smart.dto.UserUpdateRequest;
import com.parking.smart.entity.User;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     */
    PageResult<User> getUsers(Integer page, Integer size, String keyword, String role);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 管理员更新用户信息
     */
    void updateUser(Long id, UserUpdateRequest request);

    /**
     * 更新当前用户个人资料
     */
    void updateProfile(UserUpdateRequest request);

    /**
     * 修改密码
     */
    void updatePassword(PasswordUpdateRequest request);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(Long id);
}
