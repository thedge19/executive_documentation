package com.executive_documentation.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${dromara.x-file-storage.minio[0].end-point}")
    private String endpoint;

    @Value("${dromara.x-file-storage.minio[0].access-key}")
    private String accessKey;

    @Value("${dromara.x-file-storage.minio[0].secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
