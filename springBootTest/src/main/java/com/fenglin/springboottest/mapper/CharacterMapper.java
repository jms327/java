package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Character;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CharacterMapper extends BaseMapper<Character> {
}
