package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 名角档案：收录 JOJO 系列中极具代表性的角色（如贯穿系列的宿敌迪奥·布兰度）。
 * <p>
 * 与「替身图鉴（替身）」「冒险档案（篇章）」互为补充，聚焦「人物」本身：
 * 设定、性格、吸血鬼能力、替身、经典语录与名场面。
 * 列名刻意避开 MySQL 保留字（不使用 character / order / key 等作为列名）。
 */
@Data
@TableName("jojo_character")
public class Character {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 中文名 / 译名 */
    private String name;

    /** 原名 / 罗马音（如 Dio Brando / DIO） */
    private String nameOriginal;

    /** 别名 / 称号（多个用「·」分隔） */
    private String alias;

    /** 定位（如 终极反派 · 贯穿系列的宿敌） */
    private String roleType;

    /** 活跃年代 */
    private String era;

    /** 国籍 */
    private String nationality;

    /** 初登场篇章 */
    private String debut;

    /** 性格描述 */
    private String personality;

    /** 吸血鬼 / 特殊能力（多个用「、」分隔） */
    private String vampireAbility;

    /** 替身名（中文） */
    private String standName;

    /** 替身原名（英文 / 罗马音） */
    private String standOriginal;

    /** 经典语录（多条用换行分隔） */
    private String quotes;

    /** 人物简介 / 生平 */
    private String biography;

    /** 名场面 / 高光时刻 */
    private String highlight;

    /** 展示顺序（升序） */
    private Integer sort;
}
