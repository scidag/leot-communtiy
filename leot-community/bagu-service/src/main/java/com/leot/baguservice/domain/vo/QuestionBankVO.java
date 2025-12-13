package com.leot.baguservice.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题库视图对象
 */
@Data
public class QuestionBankVO implements Serializable {

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
     * 描述
     */
    private String description;

    /**
     * 图片
     */
    private String picture;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 创建者名称
     */
    private String userName;

    /**
     * 浏览量
     */
    private Integer viewNum;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
