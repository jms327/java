package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@TableName("jojo_quest")
public class Quest {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务等级 D / C / B / A。
     * rank 是 MySQL 8.0 保留字，必须加反引号，否则 MyBatis-Plus 生成的
     * SELECT / WHERE 语句会报 1064 语法错误。
     */
    @TableField("`rank`")
    private String rank;

    private String title;

    /** 任务描述；JSON 中以 desc 输出，对齐前端字段名；同时兼容输入 description */
    @JsonProperty("desc")
    @JsonAlias("description")
    private String description;

    private Integer sort;
}
