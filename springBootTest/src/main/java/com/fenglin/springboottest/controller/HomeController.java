package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.NavItem;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.service.HotKeywordService;
import com.fenglin.springboottest.service.NavService;
import com.fenglin.springboottest.service.QuestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final NavService navService;
    private final HotKeywordService hotKeywordService;
    private final AuthController authController;

    public HomeController(QuestService questService,
                          NavService navService,
                          HotKeywordService hotKeywordService,
                          AuthController authController) {
        this.questService = questService;
        this.navService = navService;
        this.hotKeywordService = hotKeywordService;
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
     * 任务详情：GET /api/quests/{id}
     * 供首页「查看详情」弹层使用，任务不存在时返回 code = 1。
     */
    @GetMapping("/quests/{id}")
    public Result<Quest> questDetail(@PathVariable("id") Long id,
                                     @RequestHeader(value = "Authorization", required = false) String auth) {
        if (authController.userIdFromAuth(auth) == null) {
            return Result.fail("令牌无效或已过期");
        }
        Quest quest = questService.getById(id);
        if (quest == null) {
            return Result.fail("任务不存在或已被移除");
        }
        return Result.ok(quest);
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

    /**
     * 搜索联想：GET /api/search/suggest?q=<keyword>
     * 只匹配标题（含等级完全匹配），最多 8 条，供搜索框下拉使用。
     */
    @GetMapping("/search/suggest")
    public Result<List<Quest>> suggest(@RequestParam(value = "q", required = false) String q,
                                       @RequestHeader(value = "Authorization", required = false) String auth) {
        if (authController.userIdFromAuth(auth) == null) {
            return Result.fail("令牌无效或已过期");
        }
        return Result.ok(questService.suggest(q, 8));
    }

    /**
     * 首页热门关键词：GET /api/hot-keywords
     * 公开接口（不含用户数据），返回 ['杜王町', '黄金精神', ...]。
     */
    @GetMapping("/hot-keywords")
    public Result<List<String>> hotKeywords() {
        return Result.ok(hotKeywordService.listTexts(8));
    }

    /** 首页导航菜单（由 jojo_nav_item 表下发，可在后台维护） */
    @GetMapping("/nav")
    public Result<List<NavItem>> nav(@RequestHeader(value = "Authorization", required = false) String auth) {
        return Result.ok(navService.listNav());
    }
}
