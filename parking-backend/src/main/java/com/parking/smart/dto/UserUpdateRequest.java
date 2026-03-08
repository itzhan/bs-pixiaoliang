package com.parking.smart.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {

    private String realName;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    private String role;

    private Integer status;
}
