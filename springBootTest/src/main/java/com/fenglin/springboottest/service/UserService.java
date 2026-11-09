package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.dto.AuthVO;
import com.fenglin.springboottest.dto.AdminLoginRequest;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.dto.UserVO;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 注册：用户名查重、两次密码校验、BCrypt 落库；邮箱为可选项 */
    public AuthVO register(RegisterRequest req) {
        if (isBlank(req.getUsername()) || isBlank(req.getPassword())) {
            throw new IllegalArgumentException("替身使者名与波纹密码均不能为空");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("两次波纹密码不一致");
        }
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", req.getUsername())) > 0) {
            throw new IllegalArgumentException("该替身使者名已被觉醒");
        }
        // 邮箱为可选项：仅当填写时才做唯一性校验
        String email = req.getEmail();
        if (!isBlank(email)
                && userMapper.selectCount(new QueryWrapper<User>().eq("email", email)) > 0) {
            throw new IllegalArgumentException("该邮箱已绑定替身");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(isBlank(email) ? null : email);
        user.setPassword(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        // 显式指定角色，避免响应中的 role 为 null（库默认值同样是 USER）
        user.setRole("USER");
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return toAuthVO(user);
    }

    /** 登录：account 支持用户名或邮箱 */
    public AuthVO login(LoginRequest req) {
        return toAuthVO(authenticate(req.getAccount(), req.getPassword()));
    }

    /**
     * 管理员登录：校验账号密码，并要求角色为 ADMIN。
     * 供后台管理系统登录页使用（仅登录，无注册）。
     */
    public AuthVO adminLogin(AdminLoginRequest req) {
        User user = authenticate(req.getAccount(), req.getPassword());
        if (!"ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("该账号无后台管理权限");
        }
        return toAuthVO(user);
    }

    /** 校验账号密码，返回用户；失败抛异常 */
    private User authenticate(String account, String password) {
        String acc = account == null ? "" : account.trim();
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username", acc)
                .or()
                .eq("email", acc));
        if (user == null || !BCrypt.checkpw(password == null ? "" : password, user.getPassword())) {
            throw new IllegalArgumentException("替身使者名或波纹密码错误");
        }
        return user;
    }

    /** 根据 id 查询用户信息（供 /api/me 使用） */
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        return user == null ? null : toVO(user);
    }

    /** 用户总数（供后台概览统计） */
    public long count() {
        return userMapper.selectCount(null);
    }

    private AuthVO toAuthVO(User user) {
        String role = user.getRole() == null ? "USER" : user.getRole();
        String token = jwtUtil.generate(user.getId(), user.getUsername(), role);
        return AuthVO.of(token, toVO(user));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
