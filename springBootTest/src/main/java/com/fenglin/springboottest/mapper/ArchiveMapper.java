package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Archive;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArchiveMapper extends BaseMapper<Archive> {
}
