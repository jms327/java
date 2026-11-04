package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.NavItem;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.service.QuestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页接口：任务列表展示、关键词搜索、导航菜单。
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    private final QuestService questService;
    private final AuthController authController;

    public HomeController(QuestService questService, AuthController authController) {
        this.questService = questService;
        this.authController = authController;
    }

    /** 首页任务卡片列表 */
    @GetMapping("/quests")
    public Result<List<Quest>> quests(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (authController.userIdFromAuth(auth) == null) {
            return Result.fail("令牌无效或已过期");
        }
        return Result.ok(questService.listAll());
    }

    /**
     * 搜索：GET /api/search?q=<keyword>
     * 匹配任务标题、描述（模糊）或等级（完全匹配，不区分大小写）。
     */
    @GetMapping("/search")
    public Result<List<Quest>> search(@RequestParam(value = "q", required = false) String q,
                                      @RequestHeader(value = "Authorization", required = false) String auth) {
        if (authController.userIdFromAuth(auth) == null) {
            return Result.fail("令牌无效或已过期");
        }
        return Result.ok(questService.search(q));
    }

    /** 首页导航菜单 */
    @GetMapping("/nav")
    public Result<List<NavItem>> nav(@RequestHeader(value = "Authorization", required = false) String auth) {
        return Result.ok(List.of(
                new NavItem("home", "首 页"),
                new NavItem("quests", "奇妙任务"),
                new NavItem("stands", "替身图鉴"),
                new NavItem("archive", "冒险档案")
        ));
    }
}
