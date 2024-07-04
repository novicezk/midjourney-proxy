package com.github.novicezk.midjourney;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "oss.config")
@Data
public class OssProperties {
    /**
     * 访问KEY ID.
     */
    private String accessKeyId;
    /**
     * 访问KEY密钥.
     */
    private String accessKeySecret;
    /**
     * 节点地址.
     */
    private String endpoint;
    /**
     * 存储桶名称.
     */
    private String bucketName;
    /**
     * 存储文件夹路径.
     */
    private String prefixPath;
    /**
     * 是否使用上传至阿里云oss存储.
     */
    private Boolean ossSave = false;
    /**
     * 是否使用签名URL.
     */
    private Boolean toSign = false;
    /**
     * 预签名访问链接过期时间，默认过期时间3000秒
     */
    private Integer expirationSeconds = 3000;
}