package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminLoginRequest;
import com.fenglin.springboottest.dto.AuthVO;
import com.fenglin.springboottest.entity.Archive;
import com.fenglin.springboottest.entity.Character;
import com.fenglin.springboottest.entity.HotKeyword;
import com.fenglin.springboottest.entity.NavMenuItem;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.entity.Stand;
import com.fenglin.springboottest.service.ArchiveService;
import com.fenglin.springboottest.service.CharacterService;
import com.fenglin.springboottest.service.HotKeywordService;
import com.fenglin.springboottest.service.NavService;
import com.fenglin.springboottest.service.QuestService;
import com.fenglin.springboottest.service.StandService;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理系统接口（/api/admin）。
 * <p>
 * 登录仅面向 ADMIN 角色；其余接口统一校验管理员令牌（JWT 中 role = ADMIN）。
 * 管理范围即首页展示的数据：
 * <ul>
 *   <li>奇妙任务 jojo_quest —— 卡片列表、搜索、详情的数据源</li>
 *   <li>热门关键词 jojo_hot_keyword —— 搜索框下方「热门」标签</li>
 *   <li>导航菜单 jojo_nav_item —— 首页顶部导航栏</li>
 *   <li>替身图鉴 jojo_stand —— 首页「替身图鉴」区块（公开浏览）</li>
 *   <li>冒险档案 jojo_archive —— 首页「冒险档案」区块（公开浏览）</li>
 *   <li>名角档案 jojo_character —— 首页「名角档案」区块（公开浏览）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final QuestService questService;
    private final HotKeywordService hotKeywordService;
    private final NavService navService;
    private final CharacterService characterService;
    private final StandService standService;
    private final ArchiveService archiveService;
    private final JwtUtil jwtUtil;

    public AdminController(UserService userService,
                           QuestService questService,
                           HotKeywordService hotKeywordService,
                           NavService navService,
                           CharacterService characterService,
                           StandService standService,
                           ArchiveService archiveService,
                           JwtUtil jwtUtil) {
        this.userService = userService;
        this.questService = questService;
        this.hotKeywordService = hotKeywordService;
        this.navService = navService;
        this.characterService = characterService;
        this.standService = standService;
        this.archiveService = archiveService;
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

    // ========================================================================
    // 概览
    // ========================================================================

    /** 后台首页统计卡片数据 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("questCount", questService.listAll().size());
        data.put("keywordCount", hotKeywordService.listOrdered().size());
        data.put("navCount", navService.listAll().size());
        data.put("characterCount", characterService.listAll().size());
        data.put("standCount", standService.listAll().size());
        data.put("archiveCount", archiveService.listAll().size());
        data.put("userCount", userService.count());
        return Result.ok(data);
    }

    // ========================================================================
    // 奇妙任务
    // ========================================================================

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
        if (isBlank(quest.getTitle())) {
            return Result.fail("任务标题不能为空");
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

    // ========================================================================
    // 热门关键词（首页搜索框下方「热门」标签）
    // ========================================================================

    @GetMapping("/hot-keywords")
    public Result<List<HotKeyword>> listKeywords(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        return Result.ok(hotKeywordService.listOrdered());
    }

    @PostMapping("/hot-keywords")
    public Result<HotKeyword> createKeyword(@RequestBody HotKeyword item,
                                            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        try {
            return Result.ok(hotKeywordService.create(item));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/hot-keywords/{id}")
    public Result<HotKeyword> updateKeyword(@PathVariable("id") Long id,
                                            @RequestBody HotKeyword item,
                                            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        item.setId(id);
        try {
            return Result.ok(hotKeywordService.update(item));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/hot-keywords/{id}")
    public Result<Void> deleteKeyword(@PathVariable("id") Long id,
                                      @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        hotKeywordService.remove(id);
        return Result.ok();
    }

    // ========================================================================
    // 导航菜单（首页顶部导航栏）
    // ========================================================================

    @GetMapping("/nav")
    public Result<List<NavMenuItem>> listNav(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        return Result.ok(navService.listAll());
    }

    @PostMapping("/nav")
    public Result<NavMenuItem> createNav(@RequestBody NavMenuItem item,
                                         @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        if (isBlank(item.getItemKey()) || isBlank(item.getLabel())) {
            return Result.fail("菜单标识与展示文案不能为空");
        }
        return Result.ok(navService.create(item));
    }

    @PutMapping("/nav/{id}")
    public Result<NavMenuItem> updateNav(@PathVariable("id") Long id,
                                         @RequestBody NavMenuItem item,
                                         @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        if (isBlank(item.getItemKey()) || isBlank(item.getLabel())) {
            return Result.fail("菜单标识与展示文案不能为空");
        }
        item.setId(id);
        return Result.ok(navService.update(item));
    }

    @DeleteMapping("/nav/{id}")
    public Result<Void> deleteNav(@PathVariable("id") Long id,
                                  @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        navService.remove(id);
        return Result.ok();
    }

    // ========================================================================
    // 名角档案（首页「名角档案」区块，公开浏览，后台可增删改）
    // ========================================================================

    @GetMapping("/characters")
    public Result<List<Character>> listCharacters(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        return Result.ok(characterService.listAll());
    }

    @PostMapping("/characters")
    public Result<Character> createCharacter(@RequestBody Character character,
                                            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        if (isBlank(character.getName())) {
            return Result.fail("角色名称不能为空");
        }
        return Result.ok(characterService.create(character));
    }

    @PutMapping("/characters/{id}")
    public Result<Character> updateCharacter(@PathVariable("id") Long id,
                                            @RequestBody Character character,
                                            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        character.setId(id);
        return Result.ok(characterService.update(character));
    }

    @DeleteMapping("/characters/{id}")
    public Result<Void> deleteCharacter(@PathVariable("id") Long id,
                                       @RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return Result.fail("无权限或登录已过期");
        }
        characterService.remove(id);
        return Result.ok();
    }

    /** 校验管理员令牌：解析 role 必须为 ADMIN */
    private boolean isAdmin(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return false;
        }
        return "ADMIN".equals(jwtUtil.parseRole(auth.substring(7).trim()));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
