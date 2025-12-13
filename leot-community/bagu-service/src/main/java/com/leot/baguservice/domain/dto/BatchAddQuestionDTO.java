package com.leot.baguservice.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量添加题目到题库请求参数
 */
@Data
public class BatchAddQuestionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题库ID
     */
    @NotNull(message = "题库ID不能为空")
    private Long questionBankId;

    /**
     * 题目ID列表
     */
    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> questionIds;
}
