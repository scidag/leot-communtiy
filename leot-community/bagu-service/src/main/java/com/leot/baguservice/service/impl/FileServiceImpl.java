package com.leot.baguservice.service.impl;

import com.leot.baguservice.service.FileService;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.config.CosConfig;
import com.leot.leotcommon.exception.BusinessException;
import com.leot.leotcommon.utils.CosUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.IOException;

/**
 * 文件服务实现类
 * 实现图片上传到腾讯云 COS 功能
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    
    @Autowired(required = false)
    private COSClient cosClient;
    
    @Resource
    private CosConfig cosConfig;
    
    @Override
    public String uploadImage(MultipartFile file, String bizType) {
        // 1. 检查 COS 客户端是否可用
        if (cosClient == null) {
            log.error("COS 客户端未初始化，文件服务不可用");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件服务暂不可用");
        }
        
        // 2. 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "上传文件不能为空");
        }
        
        // 3. 验证文件类型
        String contentType = file.getContentType();
        if (!CosUtils.isValidImageType(contentType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                    "不支持的文件类型，仅支持 jpg/png/gif/webp");
        }

        
        // 4. 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = CosUtils.getFileExtension(originalFilename);
        if (!CosUtils.isValidImageExtension(extension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                    "不支持的文件类型，仅支持 jpg/png/gif/webp");
        }
        
        // 5. 验证文件大小
        long fileSize = file.getSize();
        if (!CosUtils.isValidFileSize(fileSize)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                    "文件大小不能超过 5MB");
        }
        
        // 6. 生成唯一文件名和存储路径
        String uniqueFilename = CosUtils.generateUniqueFilename(originalFilename);
        String filePath = CosUtils.generateFilePath(bizType, uniqueFilename);
        
        // 7. 上传文件到 COS
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(contentType);
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosConfig.getBucket(),
                    filePath,
                    file.getInputStream(),
                    metadata
            );
            
            PutObjectResult result = cosClient.putObject(putObjectRequest);
            log.info("文件上传成功, path: {}, etag: {}", filePath, result.getETag());
            
        } catch (IOException e) {
            log.error("文件上传失败, path: {}, error: {}", filePath, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败，请稍后重试");
        } catch (Exception e) {
            log.error("COS 上传异常, path: {}, error: {}", filePath, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败，请稍后重试");
        }
        
        // 8. 组合并返回完整的访问 URL
        return CosUtils.combineUrl(cosConfig.getHost(), filePath);
    }
}
