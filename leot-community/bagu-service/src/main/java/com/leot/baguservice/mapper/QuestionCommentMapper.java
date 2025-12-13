package com.leot.baguservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leot.baguservice.domain.pojo.QuestionComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【question_comment(题目评论)】的数据库操作Mapper
 */
@Mapper
public interface QuestionCommentMapper extends BaseMapper<QuestionComment> {

    /**
     * 获取题目下的所有评论（包含回复）
     * @param questionId 题目ID
     * @return 评论列表
     */
    List<QuestionComment> selectCommentsByQuestionId(@Param("questionId") Long questionId);

    /**
     * 更新评论点赞数
     * @param id 评论ID
     * @param delta 变化量（正数增加，负数减少）
     */
    void updateThumbNum(@Param("id") Long id, @Param("delta") int delta);
}
