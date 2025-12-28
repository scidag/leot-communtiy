package com.leot.baguservice.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 * 提供文件上传功能
 */
public interface FileService {
    
    /**
     * 上传图片到 COS
     * 
     * @param file 图片文件
     * @param bizType 业务类型（如 questionbank）
     * @return 图片访问 URL
     */
    String uploadImage(MultipartFile file, String bizType);
}
