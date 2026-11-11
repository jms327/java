package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.entity.Archive;
import com.fenglin.springboottest.mapper.ArchiveMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchiveService {

    private final ArchiveMapper archiveMapper;

    public ArchiveService(ArchiveMapper archiveMapper) {
        this.archiveMapper = archiveMapper;
    }

    /** 冒险档案列表，按 sort 升序 */
    public List<Archive> listAll() {
        return archiveMapper.selectList(new QueryWrapper<Archive>().orderByAsc("sort"));
    }

    /** 按 id 查询档案详情，不存在返回 null */
    public Archive getById(Long id) {
        return id == null ? null : archiveMapper.selectById(id);
    }
}
