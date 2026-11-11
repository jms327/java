package com.fenglin.springboottest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 首页导航项（对外精简字段，仅首页渲染所需） */
@Data
@AllArgsConstructor
public class NavItem {
    private String key;
    private String label;
    /** 图标（emoji 或单字符） */
    private String icon;
    /** 跳转目标：页内锚点为区块 id，外链以 http(s) 开头，留空仅占位 */
    private String route;
    /** 副标题 / 悬浮提示，可空 */
    private String description;
}
