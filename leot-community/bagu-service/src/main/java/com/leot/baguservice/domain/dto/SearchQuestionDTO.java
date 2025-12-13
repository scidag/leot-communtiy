package com.leot.baguservice.domain.dto;

import com.leot.leotcommon.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索题目请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchQuestionDTO extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题库ID
     */
    private Long questionBankId;
}
