package com.leot.leotcommon.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 客户端配置类
 * 当配置了 cos.client.secretId 时才创建 COSClient Bean
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "cos.client", name = "secretId")
public class CosClientConfig {
    
    /**
     * 创建 COSClient Bean
     * 使用 @ConditionalOnProperty 实现配置缺失时的优雅降级
     * 
     * @param cosConfig COS 配置
     * @return COSClient 实例
     */
    @Bean
    public COSClient cosClient(CosConfig cosConfig) {
        log.info("初始化腾讯云 COS 客户端, region: {}, bucket: {}", 
                cosConfig.getRegion(), cosConfig.getBucket());
        
        // 创建凭证
        COSCredentials cred = new BasicCOSCredentials(
                cosConfig.getSecretId(),
                cosConfig.getSecretKey()
        );
        
        // 创建客户端配置
        ClientConfig clientConfig = new ClientConfig(
                new Region(cosConfig.getRegion())
        );
        
        return new COSClient(cred, clientConfig);
    }
}
