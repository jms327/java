package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.mapper.QuestMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestService {

    private final QuestMapper questMapper;

    public QuestService(QuestMapper questMapper) {
        this.questMapper = questMapper;
    }

    /** 首页任务列表，按 sort 升序 */
    public List<Quest> listAll() {
        return questMapper.selectList(new QueryWrapper<Quest>().orderByAsc("sort"));
    }

    /** 按 id 查询任务详情，不存在返回 null */
    public Quest getById(Long id) {
        return id == null ? null : questMapper.selectById(id);
    }

    /**
     * 搜索：标题/描述模糊匹配，或等级完全匹配（不区分大小写）。
     * 关键词为空时返回全部列表。
     */
    public List<Quest> search(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return listAll();
        }
        QueryWrapper<Quest> wrapper = new QueryWrapper<Quest>()
                .like("title", kw)
                .or()
                .like("description", kw)
                .or()
                .apply("LOWER(`rank`) = LOWER({0})", kw)
                .orderByAsc("sort");
        return questMapper.selectList(wrapper);
    }

    /**
     * 搜索联想：只匹配标题（含等级完全匹配），按 sort 升序，最多 limit 条。
     * 关键词为空时返回空列表，前端此时展示搜索历史。
     */
    public List<Quest> suggest(String keyword, int limit) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return List.of();
        }
        int size = Math.max(1, Math.min(limit, 20));
        QueryWrapper<Quest> wrapper = new QueryWrapper<Quest>()
                .like("title", kw)
                .or()
                .apply("LOWER(`rank`) = LOWER({0})", kw)
                .orderByAsc("sort")
                .last("LIMIT " + size);
        return questMapper.selectList(wrapper);
    }

    // ===== 后台管理：增删改 =====

    /** 新增任务（id 由数据库自增） */
    public Quest create(Quest quest) {
        quest.setId(null);
        questMapper.insert(quest);
        return quest;
    }

    /** 按 id 更新任务 */
    public Quest update(Quest quest) {
        questMapper.updateById(quest);
        return quest;
    }

    /** 按 id 删除任务 */
    public void remove(Long id) {
        questMapper.deleteById(id);
    }
}
