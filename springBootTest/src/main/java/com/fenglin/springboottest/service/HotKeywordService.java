package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.entity.HotKeyword;
import com.fenglin.springboottest.mapper.HotKeywordMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页「热门搜索」关键词：首页读取 + 后台维护。
 */
@Service
public class HotKeywordService {

    private final HotKeywordMapper hotKeywordMapper;

    public HotKeywordService(HotKeywordMapper hotKeywordMapper) {
        this.hotKeywordMapper = hotKeywordMapper;
    }

    /** 首页：按 weight 降序、sort 升序取前 limit 条，只返回关键词文本 */
    public List<String> listTexts(int limit) {
        int size = Math.max(1, Math.min(limit, 20));
        return listOrdered().stream()
                .limit(size)
                .map(HotKeyword::getKeyword)
                .collect(Collectors.toList());
    }

    /** 后台：完整记录，按 weight 降序、sort 升序 */
    public List<HotKeyword> listOrdered() {
        return hotKeywordMapper.selectList(
                new QueryWrapper<HotKeyword>().orderByDesc("weight").orderByAsc("sort").orderByAsc("id"));
    }

    // ===== 后台管理：增删改 =====

    /** 新增；关键词重复时抛 IllegalArgumentException，由控制器转为友好提示 */
    public HotKeyword create(HotKeyword item) {
        String keyword = normalized(item.getKeyword());
        if (exists(keyword, null)) {
            throw new IllegalArgumentException("关键词「" + keyword + "」已存在");
        }
        item.setId(null);
        item.setKeyword(keyword);
        if (item.getWeight() == null) {
            item.setWeight(0);
        }
        if (item.getSort() == null) {
            item.setSort(listOrdered().size() + 1);
        }
        hotKeywordMapper.insert(item);
        return item;
    }

    public HotKeyword update(HotKeyword item) {
        String keyword = normalized(item.getKeyword());
        if (exists(keyword, item.getId())) {
            throw new IllegalArgumentException("关键词「" + keyword + "」已存在");
        }
        item.setKeyword(keyword);
        hotKeywordMapper.updateById(item);
        return item;
    }

    public void remove(Long id) {
        hotKeywordMapper.deleteById(id);
    }

    private boolean exists(String keyword, Long excludeId) {
        QueryWrapper<HotKeyword> wrapper = new QueryWrapper<HotKeyword>().eq("keyword", keyword);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        return hotKeywordMapper.selectCount(wrapper) > 0;
    }

    private String normalized(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            throw new IllegalArgumentException("关键词不能为空");
        }
        if (kw.length() > 64) {
            throw new IllegalArgumentException("关键词最长 64 个字符");
        }
        return kw;
    }
}
