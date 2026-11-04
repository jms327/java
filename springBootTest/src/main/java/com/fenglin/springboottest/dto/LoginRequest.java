package com.fenglin.springboottest.dto;

import lombok.Data;

/** 登录请求：account 支持用户名或邮箱 */
@Data
public class LoginRequest {
    private String account;
    private String password;
}
