package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 首页热门搜索关键词。
 * 首页搜索框下方的「热门」标签由该表下发，后台可直接调整权重与顺序。
 */
@Data
@TableName("jojo_hot_keyword")
public class HotKeyword {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关键词文本，如「杜王町」 */
    private String keyword;

    /** 权重，越大越靠前 */
    private Integer weight;

    /** 同权重时的展示顺序（升序） */
    private Integer sort;
}
