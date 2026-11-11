package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.dto.NavItem;
import com.fenglin.springboottest.entity.NavMenuItem;
import com.fenglin.springboottest.mapper.NavItemMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页导航栏菜单：首页读取 + 后台维护。
 */
@Service
public class NavService {

    private final NavItemMapper navItemMapper;

    public NavService(NavItemMapper navItemMapper) {
        this.navItemMapper = navItemMapper;
    }

    /** 后台：完整菜单记录（含 id、sort），按 sort 升序 */
    public List<NavMenuItem> listAll() {
        return navItemMapper.selectList(new QueryWrapper<NavMenuItem>().orderByAsc("sort").orderByAsc("id"));
    }

    /** 首页：精简字段，含 icon / route / description；表为空时回退到内置菜单 */
    public List<NavItem> listNav() {
        List<NavMenuItem> rows = listAll();
        if (rows.isEmpty()) {
            return defaultNav();
        }
        return rows.stream()
                .map(r -> new NavItem(
                        r.getItemKey(),
                        r.getLabel(),
                        r.getIcon(),
                        r.getRoute(),
                        r.getDescription()))
                .collect(Collectors.toList());
    }

    /** 首页内置菜单（导航表为空时的兜底） */
    public static List<NavItem> defaultNav() {
        return List.of(
                new NavItem("home", "首 页", "🏠", "home", "回到起点"),
                new NavItem("quests", "奇妙任务", "⚔️", "quests", "承接命运委托"),
                new NavItem("stands", "替身图鉴", "👁️", "stands", "觉醒你的替身"),
                new NavItem("archive", "冒险档案", "📜", "archive", "记录每一段旅程")
        );
    }

    // ===== 后台管理：增删改 =====

    public NavMenuItem create(NavMenuItem item) {
        item.setId(null);
        if (item.getSort() == null) {
            item.setSort(listAll().size() + 1);
        }
        navItemMapper.insert(item);
        return item;
    }

    public NavMenuItem update(NavMenuItem item) {
        navItemMapper.updateById(item);
        return item;
    }

    public void remove(Long id) {
        navItemMapper.deleteById(id);
    }
}
