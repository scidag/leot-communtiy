package com.leot.leotcommon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯云 COS 配置类
 * 从 application.yaml 中读取 cos.client 前缀的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "cos.client")
public class CosConfig {
    
    /**
     * COS 访问域名
     */
    private String host;
    
    /**
     * 腾讯云 SecretId
     */
    private String secretId;
    
    /**
     * 腾讯云 SecretKey
     */
    private String secretKey;
    
    /**
     * 存储桶地域（如 ap-beijing）
     */
    private String region;
    
    /**
     * 存储桶名称
     */
    private String bucket;
}
