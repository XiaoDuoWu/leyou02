package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Component
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties{
    private String pubKeyPath;// 公钥路径
    private String priKeyPath;// 私钥路径
    private int expire;// 过期时间
    private String cookieName; //cookie的名称啊
    private PublicKey publicKey;// 公钥
    private PrivateKey privateKey;// 私钥

    @PostConstruct
    public void init() throws Exception{
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        }catch (Exception e){
            log.error("加载公钥和私钥失败！", e);
            throw new Exception(e);
        }
    }

}
