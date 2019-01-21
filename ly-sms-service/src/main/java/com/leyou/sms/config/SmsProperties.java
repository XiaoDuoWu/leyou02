package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ly.sms")
public class SmsProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String connectTimeout;
    private String readTimeout;
    private String signName;
    private String verifyTemplateCode;
}