package com.fenglin.springboottest.dto;

import lombok.Data;

/** 后台管理员登录请求（仅登录，无注册） */
@Data
public class AdminLoginRequest {
    private String account;
    private String password;
}
