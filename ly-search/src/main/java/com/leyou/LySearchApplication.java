package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients//开启feign客户端，可以用来远程调用
public class LySearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(LySearchApplication.class, args);
    }
}