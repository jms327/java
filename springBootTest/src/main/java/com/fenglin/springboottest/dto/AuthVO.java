package com.fenglin.springboottest.dto;

import lombok.Data;

/** 登录/注册成功后的返回：token + 用户信息 */
@Data
public class AuthVO {
    private String token;
    private UserVO user;

    public static AuthVO of(String token, UserVO user) {
        AuthVO vo = new AuthVO();
        vo.token = token;
        vo.user = user;
        return vo;
    }
}
