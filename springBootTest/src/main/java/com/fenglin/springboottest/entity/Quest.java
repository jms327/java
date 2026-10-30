package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@TableName("jojo_quest")
public class Quest {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务等级 D / C / B / A */
    private String rank;

    private String title;

    /** 任务描述；JSON 中以 desc 输出，对齐前端字段名 */
    @JsonProperty("desc")
    private String description;

    private Integer sort;
}
