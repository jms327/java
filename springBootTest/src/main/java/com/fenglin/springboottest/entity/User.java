package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("jojo_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    /**
     * 波纹密码，存储 BCrypt 哈希。
     * password 是 MySQL 8.0 保留字，必须加反引号，否则 MyBatis-Plus 生成的
     * SELECT / WHERE / SET 语句会报 1064 语法错误。
     */
    @TableField("`password`")
    private String password;

    /** 觉醒时间 */
    private LocalDateTime createTime;

    /**
     * 角色：USER（普通替身使者）/ ADMIN（后台管理员）。
     * 用于后台管理系统接口的权限校验。
     */
    @TableField("`role`")
    private String role;
}
