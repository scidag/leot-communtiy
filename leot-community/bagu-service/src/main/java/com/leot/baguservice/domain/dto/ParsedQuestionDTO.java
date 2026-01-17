package com.leot.baguservice.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * PDF解析出的题目数据
 */
@Data
public class ParsedQuestionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 题目答案
     */
    private String answer;
}
