package com.leyou.sms;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听器 监听收到消息后  发送信息
 */
@Component
public class SmsListener {
    @Autowired
    private SmsProperties prop;
    @Autowired
    private SmsUtils smsUtils;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ly.sms.verify.queue"),
            exchange = @Exchange(name = "ly.sms.exchange", type = ExchangeTypes.TOPIC),
            key = "sms.verify.code"
    ))
//    监听方法 rabbit发送消息后 处理消息
    public void listenVerifyCode(Map<String, String> msg) {
        if (msg == null) {
            return;
        }
//        获取并移除phone这个键值对   此时map中只有一个键值对
        String phone = msg.remove("phone");
        if (StringUtils.isBlank(phone)) {
            return;

        }
        SendSmsResponse sendSmsResponse = smsUtils.sendSms(phone,prop.getSignName() ,prop.getVerifyTemplateCode(), JsonUtils.toString(msg));
        System.err.println("sendSmsResponse = " + sendSmsResponse.getMessage());
    }

    /**
     * 发送注册时的短信验证码
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue", durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange", type = ExchangeTypes.TOPIC),
            key = "register.verify.code"
    ))
    public void listenVerifyCode2(Map<String,String> msg) {
        if(msg != null && msg.containsKey("phone")){
            // 获取并移除phone的值
            String phone = msg.remove("phone");
            // 校验数据是否合适
            if(phone.matches("^1[35789]\\d{9}$")){
                // 发送短信
                smsUtils.sendSms(phone, prop.getSignName(), prop.getVerifyTemplateCode(), JsonUtils.toString(msg));
            }
        }
    }

}
