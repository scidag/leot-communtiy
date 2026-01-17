package com.leot.baguservice.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.util.ObjUtil;
import com.leot.baguservice.domain.dto.BatchImportDTO;
import com.leot.baguservice.domain.vo.BatchImportResultVO;
import com.leot.baguservice.domain.vo.PdfParseResultVO;
import com.leot.baguservice.service.PdfParseService;
import com.leot.baguservice.service.QuestionImportService;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import com.leot.leotcommon.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF导入控制器
 */
@RestController
@RequestMapping("/question/import")
public class PdfImportController {

    @Resource
    private PdfParseService pdfParseService;

    @Resource
    private QuestionImportService questionImportService;

    /**
     * 解析PDF文件，提取题目列表
     *
     * @param file PDF文件
     * @return 解析结果
     */
    @PostMapping("/parse")
    @SaCheckRole("admin")
    public BaseResponse<PdfParseResultVO> parsePdf(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请选择要上传的PDF文件");
        }
        PdfParseResultVO result = pdfParseService.parsePdf(file);
        return ResultUtil.success(result);
    }

    /**
     * 批量导入题目到指定题库
     *
     * @param dto 批量导入请求参数
     * @return 导入结果
     */
    @PostMapping("/batch")
    @SaCheckRole("admin")
    public BaseResponse<BatchImportResultVO> batchImport(@RequestBody BatchImportDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        BatchImportResultVO result = questionImportService.batchImport(dto);
        return ResultUtil.success(result);
    }
}
