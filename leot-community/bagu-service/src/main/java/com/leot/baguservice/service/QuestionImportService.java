package com.leot.baguservice.service;

import com.leot.baguservice.domain.dto.BatchImportDTO;
import com.leot.baguservice.domain.vo.BatchImportResultVO;

/**
 * 题目导入服务接口
 */
public interface QuestionImportService {

    /**
     * 批量导入题目到指定题库
     *
     * @param dto 批量导入请求参数
     * @return 导入结果
     */
    BatchImportResultVO batchImport(BatchImportDTO dto);
}
