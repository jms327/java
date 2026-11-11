package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 冒险档案：按篇章记录每一部「JOJO 的奇妙冒险」的旅程。
 * 首页「冒险档案」区块的数据来源，公开可浏览。
 */
@Data
@TableName("jojo_archive")
public class Archive {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 篇章，如「第一部」「第六部」 */
    private String part;

    /** 档案标题，如「幻影血脉」 */
    private String title;

    /** 年代 / 舞台，如「1880s 英国 → 埃及」 */
    private String era;

    /** 主要舞台地点 */
    private String location;

    /** 主角（JOJO） */
    private String hero;

    /** 宿敌 */
    private String villain;

    /** 摘要 */
    private String summary;

    /** 名场面 / 一句话亮点（可空） */
    private String highlight;

    /** 展示顺序（升序） */
    private Integer sort;
}
