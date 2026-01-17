package com.leot.baguservice.service;

import com.leot.baguservice.domain.vo.PdfParseResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF解析服务接口
 */
public interface PdfParseService {

    /**
     * 解析PDF文件，提取题目列表
     *
     * @param file PDF文件
     * @return 解析结果
     */
    PdfParseResultVO parsePdf(MultipartFile file);
}
