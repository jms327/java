package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.entity.Character;
import com.fenglin.springboottest.mapper.CharacterMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CharacterService {

    private final CharacterMapper characterMapper;

    public CharacterService(CharacterMapper characterMapper) {
        this.characterMapper = characterMapper;
    }

    /** 名角档案列表，按 sort 升序 */
    public List<Character> listAll() {
        return characterMapper.selectList(new QueryWrapper<Character>().orderByAsc("sort"));
    }

    /** 按 id 查询，不存在返回 null */
    public Character getById(Long id) {
        return id == null ? null : characterMapper.selectById(id);
    }

    /** 新增名角档案 */
    public Character create(Character character) {
        characterMapper.insert(character);
        return character;
    }

    /** 修改名角档案 */
    public Character update(Character character) {
        characterMapper.updateById(character);
        return character;
    }

    /** 删除名角档案 */
    public void remove(Long id) {
        characterMapper.deleteById(id);
    }
}
