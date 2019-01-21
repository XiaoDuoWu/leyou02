package com.leyou.sms.utils;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsUtils {

    private IAcsClient acsClient;

    public SmsUtils(IAcsClient acsClient) {
        this.acsClient = acsClient;
    }

    public SendSmsResponse sendSms(String phone, String singName, String templateCode, String templateParam){
        try {
            //组装请求对象
            SendSmsRequest request = new SendSmsRequest();
            //使用post提交
            request.setMethod(MethodType.POST);
            //必填:待发送手机号。
            request.setPhoneNumbers(phone);
            //必填:短信签名
            request.setSignName(singName);
            //必填:短信模板
            request.setTemplateCode(templateCode);
            //可选:模板中的变量替换JSON串
            request.setTemplateParam(templateParam);
            //请求失败这里会抛ClientException异常
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            return sendSmsResponse;
        }catch (Exception e){
            log.error("【消息服务】发送短信失败，手机号：{}", phone, e);
            return null;
        }
    }
}