package com.leyou.gateway.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
@Data
@Component
@ConfigurationProperties(prefix = "ly.cors")//将配置文件映射到对象中
public class CorsProperties {
    private List<String> allowedOrigins;
    private Boolean allowCredentials;
    private List<String> allowedMethods;
    private Long maxAge;
    private String filterPath;
    private List<String> allowedHeaders;
}
