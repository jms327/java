package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Stand;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StandMapper extends BaseMapper<Stand> {
}
