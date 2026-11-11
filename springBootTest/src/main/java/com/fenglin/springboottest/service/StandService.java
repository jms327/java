package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.entity.Stand;
import com.fenglin.springboottest.mapper.StandMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StandService {

    private final StandMapper standMapper;

    public StandService(StandMapper standMapper) {
        this.standMapper = standMapper;
    }

    /** 替身图鉴列表，按 sort 升序 */
    public List<Stand> listAll() {
        return standMapper.selectList(new QueryWrapper<Stand>().orderByAsc("sort"));
    }

    /** 按 id 查询替身详情，不存在返回 null */
    public Stand getById(Long id) {
        return id == null ? null : standMapper.selectById(id);
    }
}
