package com.leot.baguservice.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加题目到题库请求参数
 */
@Data
public class AddQuestionBankQuestionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题库ID
     */
    @NotNull(message = "题库ID不能为空")
    private Long questionBankId;

    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
}
