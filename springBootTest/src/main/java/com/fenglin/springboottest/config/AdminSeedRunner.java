package com.fenglin.springboottest.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 启动时为后台管理系统初始化一个管理员账号。
 * 仅当库中不存在任何 ADMIN 角色用户时才创建，保证幂等、可重复执行。
 * 默认账号：admin / admin123
 */
@Component
public class AdminSeedRunner implements ApplicationRunner {

    private final UserMapper userMapper;

    public AdminSeedRunner(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long adminCount = userMapper.selectCount(
                new QueryWrapper<User>().eq("`role`", "ADMIN"));
        if (adminCount != null && adminCount > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@jojo.com");
        admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
        admin.setRole("ADMIN");
        admin.setCreateTime(LocalDateTime.now());
        userMapper.insert(admin);
    }
}
