package com.leyou.sms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class SmsListenerTest {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void testMessage()throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("phone", "15720573526");
        map.put("code", "666666");
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", map);
    }
}