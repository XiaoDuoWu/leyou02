package com.leyou.cart.listen;

import com.leyou.cart.service.CartService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CartListen {
    @Autowired
    private CartService cartService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ly.cart.delete.queue", durable = "true"),
            exchange = @Exchange(
                    name = "ly.cart.exchange", type = ExchangeTypes.TOPIC),
            key = "cart.delete"
    ))
    public void listenInsert( Map<Long, List<Long>> map) {
        if (map != null) {
            cartService.deleteCarts(map);
        }
    }
}
