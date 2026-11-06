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

    /** 首页：只需要 key + label；表为空时回退到内置菜单，保证首页不会没有导航 */
    public List<NavItem> listNav() {
        List<NavMenuItem> rows = listAll();
        if (rows.isEmpty()) {
            return defaultNav();
        }
        return rows.stream()
                .map(r -> new NavItem(r.getItemKey(), r.getLabel()))
                .collect(Collectors.toList());
    }

    /** 首页内置菜单（导航表为空时的兜底） */
    public static List<NavItem> defaultNav() {
        return List.of(
                new NavItem("home", "首 页"),
                new NavItem("quests", "奇妙任务"),
                new NavItem("stands", "替身图鉴"),
                new NavItem("archive", "冒险档案")
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
