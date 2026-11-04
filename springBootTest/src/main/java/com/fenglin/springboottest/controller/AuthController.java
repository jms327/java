package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AuthVO;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.dto.UserVO;
import com.fenglin.springboottest.service.UserService;
import com.fenglin.springboottest.util.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口：注册 / 登录 / 当前用户信息。
 * 响应结构统一为 { code, msg, data }，对齐前端 api.js。
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public Result<AuthVO> register(@RequestBody RegisterRequest req) {
        try {
            return Result.ok(userService.register(req));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<AuthVO> login(@RequestBody LoginRequest req) {
        try {
            return Result.ok(userService.login(req));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 当前登录用户，请求头携带 Authorization: Bearer <token> */
    @GetMapping("/me")
    public Result<UserVO> me(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = userIdFromAuth(auth);
        if (userId == null) {
            return Result.fail("令牌无效或已过期");
        }
        UserVO user = userService.getById(userId);
        if (user == null) {
            return Result.fail("令牌无效或已过期");
        }
        return Result.ok(user);
    }

    /** 解析 Authorization 头中的 Bearer token */
    Long userIdFromAuth(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        return jwtUtil.parseUserId(auth.substring(7).trim());
    }
}
