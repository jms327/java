package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

    /** 波纹密码，存储 BCrypt 哈希 */
    private String password;

    /** 觉醒时间 */
    private LocalDateTime createTime;
}
