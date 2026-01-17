package com.leot.baguservice.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 批量导入结果
 */
@Data
public class BatchImportResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功导入数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 总数量
     */
    private Integer totalCount;
}
