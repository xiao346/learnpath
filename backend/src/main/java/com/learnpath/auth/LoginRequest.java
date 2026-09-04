package com.learnpath.auth;

import com.learnpath.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank(message = "请输入账号") String account,
        @NotBlank(message = "请输入密码") String password,
        @NotNull(message = "请选择登录身份") UserRole role,
        boolean rememberMe
) {
}
