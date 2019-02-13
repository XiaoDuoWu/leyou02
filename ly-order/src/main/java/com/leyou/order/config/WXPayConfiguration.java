package com.leyou.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //声明这个是配置类 并把PayConfig注册到spring容器中
public class WXPayConfiguration {
    @Bean
    @ConfigurationProperties("ly.pay")
    public PayConfig payConfig(){
        return new PayConfig();
    }
}
