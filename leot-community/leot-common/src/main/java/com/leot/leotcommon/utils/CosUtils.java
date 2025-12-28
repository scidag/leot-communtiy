package com.leot.leotcommon.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 腾讯云 COS 工具类
 * 提供文件验证、路径生成、URL 组合等功能
 */
public class CosUtils {
    
    /**
     * 允许的图片 MIME 类型
     */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    
    /**
     * 允许的图片扩展名
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );
    
    /**
     * 最大文件大小：5MB
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    
    private CosUtils() {
        // 工具类禁止实例化
    }
    
    /**
     * 验证文件类型是否为允许的图片类型
     * 
     * @param contentType 文件的 MIME 类型
     * @return true 如果是允许的图片类型，否则 false
     */
    public static boolean isValidImageType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        return ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    
    /**
     * 验证文件扩展名是否为允许的图片扩展名
     * 
     * @param extension 文件扩展名（不含点号）
     * @return true 如果是允许的扩展名，否则 false
     */
    public static boolean isValidImageExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    /**
     * 验证文件大小是否在允许范围内
     * 
     * @param fileSize 文件大小（字节）
     * @return true 如果文件大小 <= 5MB，否则 false
     */
    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }
    
    /**
     * 获取最大允许的文件大小（字节）
     * 
     * @return 最大文件大小
     */
    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }
    
    /**
     * 从文件名中提取扩展名
     * 
     * @param filename 原始文件名
     * @return 扩展名（不含点号），如果没有扩展名则返回空字符串
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 生成唯一的文件名
     * 使用 UUID 生成唯一标识，保留原始文件扩展名
     * 
     * @param originalFilename 原始文件名
     * @return 唯一文件名（格式：uuid.ext）
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        if (extension.isEmpty()) {
            return uuid;
        }
        return uuid + "." + extension;
    }
    
    /**
     * 生成文件存储路径
     * 格式：images/{bizType}/{yyyy}/{MM}/{dd}/{uniqueFilename}
     * 
     * @param bizType 业务类型（如 questionbank）
     * @param uniqueFilename 唯一文件名
     * @return 完整的存储路径
     */
    public static String generateFilePath(String bizType, String uniqueFilename) {
        return generateFilePath(bizType, uniqueFilename, LocalDate.now());
    }
    
    /**
     * 生成文件存储路径（可指定日期，用于测试）
     * 格式：images/{bizType}/{yyyy}/{MM}/{dd}/{uniqueFilename}
     * 
     * @param bizType 业务类型（如 questionbank）
     * @param uniqueFilename 唯一文件名
     * @param date 日期
     * @return 完整的存储路径
     */
    public static String generateFilePath(String bizType, String uniqueFilename, LocalDate date) {
        String datePath = date.format(DATE_FORMATTER);
        return String.format("images/%s/%s/%s", bizType, datePath, uniqueFilename);
    }
    
    /**
     * 组合 COS host 和文件路径生成完整的访问 URL
     * 
     * @param host COS 访问域名（如 https://bucket.cos.region.myqcloud.com）
     * @param filePath 文件路径
     * @return 完整的访问 URL
     */
    public static String combineUrl(String host, String filePath) {
        if (host == null || host.isBlank()) {
            return filePath;
        }
        if (filePath == null || filePath.isBlank()) {
            return host;
        }
        // 移除 host 末尾的斜杠
        String normalizedHost = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
        // 移除 filePath 开头的斜杠
        String normalizedPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        return normalizedHost + "/" + normalizedPath;
    }
    
    /**
     * 获取允许的图片类型集合
     * 
     * @return 允许的 MIME 类型集合
     */
    public static Set<String> getAllowedImageTypes() {
        return ALLOWED_IMAGE_TYPES;
    }
    
    /**
     * 获取允许的图片扩展名集合
     * 
     * @return 允许的扩展名集合
     */
    public static Set<String> getAllowedExtensions() {
        return ALLOWED_EXTENSIONS;
    }
}
