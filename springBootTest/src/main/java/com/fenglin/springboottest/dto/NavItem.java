package com.fenglin.springboottest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 首页导航项 */
@Data
@AllArgsConstructor
public class NavItem {
    private String key;
    private String label;
}
