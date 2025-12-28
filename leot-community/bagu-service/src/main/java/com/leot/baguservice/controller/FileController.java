package com.leot.baguservice.controller;

import com.leot.baguservice.service.FileService;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import com.leot.leotcommon.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 提供图片上传接口
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @param bizType 业务类型（默认为 questionbank）
     * @return 图片访问 URL
     */
    @PostMapping("/upload/image")
    public BaseResponse<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", defaultValue = "questionbank") String bizType) {
        
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "上传文件不能为空");
        }
        
        log.info("开始上传图片, 原始文件名: {}, 业务类型: {}, 文件大小: {} bytes", 
                file.getOriginalFilename(), bizType, file.getSize());
        
        // 调用服务上传图片
        String imageUrl = fileService.uploadImage(file, bizType);
        
        log.info("图片上传成功, URL: {}", imageUrl);
        
        return ResultUtil.success(imageUrl);
    }
}
