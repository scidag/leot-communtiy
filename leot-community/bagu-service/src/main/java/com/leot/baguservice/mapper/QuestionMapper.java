package com.leot.baguservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leot.baguservice.domain.pojo.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 针对表【question(题目)】的数据库操作Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 增加浏览量
     * @param id 题目ID
     */
    void incrementViewNum(@Param("id") Long id);

    /**
     * 更新点赞数
     * @param id 题目ID
     * @param delta 变化量（正数增加，负数减少）
     */
    void updateThumbNum(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 更新收藏数
     * @param id 题目ID
     * @param delta 变化量（正数增加，负数减少）
     */
    void updateFavourNum(@Param("id") Long id, @Param("delta") int delta);
}
