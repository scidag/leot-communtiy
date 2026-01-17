package com.leot.baguservice.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量导入题目请求参数
 */
@Data
public class BatchImportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标题库ID
     */
    @NotNull(message = "题库ID不能为空")
    private Long questionBankId;

    /**
     * 待导入的题目列表
     */
    @NotEmpty(message = "题目列表不能为空")
    private List<ParsedQuestionDTO> questions;
}
