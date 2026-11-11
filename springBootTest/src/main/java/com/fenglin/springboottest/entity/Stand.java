package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 替身图鉴：收录替身使者觉醒出的「替身」能力档案。
 * <p>
 * 六维属性沿用 JOJO 设定：破坏力 / 速度 / 射程距离 / 持续力 / 精密度 / 成长性，
 * 取值为 A~E 或 ∞（无穷）。其中 range 是 MySQL 保留字，落库列名加反引号。
 */
@Data
@TableName("jojo_stand")
public class Stand {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 替身名（中文译名） */
    private String name;

    /** 原名（英文 / 罗马音） */
    private String originalName;

    /** 替身使者（使用者） */
    private String userName;

    /** 类型：近距离力量型 / 远距离操纵型 / 自动操作型 ... */
    private String type;

    /** 破坏力 A~E / ∞ */
    private String destroyPower;

    /** 速度 A~E / ∞ */
    private String speed;

    /** 射程距离 A~E / ∞（range 是保留字，落库列名加反引号） */
    @TableField("`range`")
    private String range;

    /** 持续力 A~E / ∞ */
    private String endurance;

    /** 精密度 A~E / ∞（precision 是保留字，落库列名加反引号） */
    @TableField("`precision`")
    private String precision;

    /** 成长性 A~E / ∞ */
    private String growth;

    /** 能力描述 */
    private String ability;

    /** 经典台词（可空） */
    private String quote;

    /** 展示顺序（升序） */
    private Integer sort;
}
