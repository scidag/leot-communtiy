package com.leot.baguservice.domain.dto;

import com.leot.leotcommon.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询题目请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryQuestionDTO extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 题库ID
     */
    private Long questionBankId;
}
