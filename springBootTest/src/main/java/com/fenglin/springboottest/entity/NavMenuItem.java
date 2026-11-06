package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 首页导航栏菜单项。
 * <p>
 * itemKey 是前端路由用的标识（home / quests / stands / archive ...），
 * label 是展示文案。后台可改文案与排序，key 一旦新增后不建议随意更改，
 * 否则首页点击后会落到「开发中」占位分支。
 */
@Data
@TableName("jojo_nav_item")
public class NavMenuItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 菜单标识；key 是 MySQL 保留字，落库列名为 item_key */
    @TableField("item_key")
    private String itemKey;

    /** 展示文案 */
    private String label;

    /** 展示顺序（升序） */
    private Integer sort;
}
