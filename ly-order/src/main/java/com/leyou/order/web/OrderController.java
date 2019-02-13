package com.leyou.order.web;

import com.leyou.common.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
//    接收类型是OrderDTO 并且@valid 注解判断格式是否符合实体类的要求格式
    public ResponseEntity<Long> createOrder(@RequestBody @Valid OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    @GetMapping("order/{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long orderId){
        return ResponseEntity.ok(orderService.queryOrderById(orderId));
    }

    @GetMapping("order/url/{orderId}")
    public ResponseEntity<String> createPayUrl(@PathVariable("orderId")Long orderId){
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }

    @GetMapping("order/state/{orderId}")
    public ResponseEntity<Integer> queryPayStatus(@PathVariable("orderId")Long orderId){
        return ResponseEntity.ok(orderService.queryPayStatus(orderId).getState());
    }

}
