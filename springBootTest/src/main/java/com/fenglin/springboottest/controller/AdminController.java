package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminLoginRequest;
import com.fenglin.springboottest.dto.AuthVO;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.service.QuestService;
import com.fenglin.springboottest.service.UserService;
import com.fenglin.springboottest.util.JwtUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理系统接口（/api/admin）。
 * 全部接口都需要管理员令牌（JWT 中 role = ADMIN），否则返回“无权限”。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final QuestService questService;
    private final JwtUtil jwtUtil;

    public AdminController(UserService userService, QuestService questService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.questService = questService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 管理员登录：仅登录，无注册。账号需具备 ADMIN 角色。
     */
    @PostMapping("/login")
    public Result<AuthVO> login(@RequestBody AdminLoginRequest req) {
        try {
            return Result.ok(userService.adminLogin(req));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 任务列表（供后台管理表格展示） */
    @GetMapping("/quests")
    public Result<List<Quest>> listQuests(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        return Result.ok(questService.listAll());
    }

    /** 新增任务 */
    @PostMapping("/quests")
    public Result<Quest> createQuest(@RequestBody Quest quest,
                                     @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        return Result.ok(questService.create(quest));
    }

    /** 修改任务 */
    @PutMapping("/quests/{id}")
    public Result<Quest> updateQuest(@PathVariable("id") Long id,
                                     @RequestBody Quest quest,
                                     @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        quest.setId(id);
        return Result.ok(questService.update(quest));
    }

    /** 删除任务 */
    @DeleteMapping("/quests/{id}")
    public Result<Void> deleteQuest(@PathVariable("id") Long id,
                                    @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        questService.remove(id);
        return Result.ok();
    }

    /** 校验管理员令牌：解析 role 必须为 ADMIN */
    private boolean isAdmin(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return false;
        }
        return "ADMIN".equals(jwtUtil.parseRole(auth.substring(7).trim()));
    }
}
