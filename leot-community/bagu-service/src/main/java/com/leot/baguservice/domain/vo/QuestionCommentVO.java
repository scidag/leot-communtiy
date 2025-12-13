package com.leot.baguservice.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目评论视图对象
 */
@Data
public class QuestionCommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 评论用户名称
     */
    private String userName;

    /**
     * 评论用户头像
     */
    private String userAvatar;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 被回复用户ID
     */
    private Long replyUserId;

    /**
     * 被回复用户名称
     */
    private String replyUserName;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 当前用户是否点赞该评论
     */
    private Boolean hasThumb;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 子评论列表（树形结构）
     */
    private List<QuestionCommentVO> children;
}
