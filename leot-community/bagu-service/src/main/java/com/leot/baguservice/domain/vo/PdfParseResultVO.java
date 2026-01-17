package com.leot.baguservice.domain.vo;

import com.leot.baguservice.domain.dto.ParsedQuestionDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * PDF解析结果
 */
@Data
public class PdfParseResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 解析出的题目列表
     */
    private List<ParsedQuestionDTO> questions;

    /**
     * 题目总数
     */
    private Integer totalCount;

    /**
     * 解析耗时（毫秒）
     */
    private Long parseTime;
}
