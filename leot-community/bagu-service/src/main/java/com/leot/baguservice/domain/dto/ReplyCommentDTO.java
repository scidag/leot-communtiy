package com.leot.baguservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 回复评论请求参数
 */
@Data
public class ReplyCommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /**
     * 父评论ID
     */
    @NotNull(message = "父评论ID不能为空")
    private Long parentId;

    /**
     * 被回复用户ID（可选）
     */
    private Long replyUserId;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容不能超过1000字符")
    private String content;
}
