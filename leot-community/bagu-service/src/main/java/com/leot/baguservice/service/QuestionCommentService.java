package com.leot.baguservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leot.baguservice.domain.dto.AddCommentDTO;
import com.leot.baguservice.domain.dto.ReplyCommentDTO;
import com.leot.baguservice.domain.pojo.QuestionComment;
import com.leot.baguservice.domain.vo.QuestionCommentVO;

import java.util.List;

/**
 * 针对表【question_comment(题目评论)】的数据库操作Service
 */
public interface QuestionCommentService extends IService<QuestionComment> {

    /**
     * 添加评论
     * @param dto 添加评论请求参数
     * @param userId 用户ID
     * @return 评论ID
     */
    Long addComment(AddCommentDTO dto, Long userId);

    /**
     * 回复评论
     * @param dto 回复评论请求参数
     * @param userId 用户ID
     * @return 评论ID
     */
    Long replyComment(ReplyCommentDTO dto, Long userId);

    /**
     * 删除评论（权限校验：本人或管理员）
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @param isAdmin 是否为管理员
     * @return 是否成功
     */
    Boolean deleteComment(Long commentId, Long userId, boolean isAdmin);

    /**
     * 获取题目评论列表（树形结构）
     * @param questionId 题目ID
     * @return 评论列表（树形结构）
     */
    List<QuestionCommentVO> listCommentByQuestionId(Long questionId);

    /**
     * 点赞/取消点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否点赞（true=点赞，false=取消点赞）
     */
    Boolean thumbComment(Long commentId, Long userId);

    /**
     * 校验评论内容
     * @param content 评论内容
     */
    void validateContent(String content);

    /**
     * 将QuestionComment转换为QuestionCommentVO
     * @param comment 评论实体
     * @return 评论视图对象
     */
    QuestionCommentVO convertToVO(QuestionComment comment);

    /**
     * 将QuestionComment转换为QuestionCommentVO（带用户点赞状态）
     * @param comment 评论实体
     * @param userId 当前用户ID（可为null）
     * @return 评论视图对象
     */
    QuestionCommentVO convertToVO(QuestionComment comment, Long userId);
}
