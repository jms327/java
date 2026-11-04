package com.fenglin.springboottest.dto;

import lombok.Data;

/** 注册请求，字段与前端 api.js 的 register payload 一致 */
@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
}
