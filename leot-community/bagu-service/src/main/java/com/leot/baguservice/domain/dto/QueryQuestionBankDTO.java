package com.leot.baguservice.domain.dto;

import com.leot.leotcommon.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询题库请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryQuestionBankDTO extends PageRequest implements Serializable {

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
     * 创建用户ID
     */
    private Long userId;
}
