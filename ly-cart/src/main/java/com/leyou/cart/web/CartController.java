package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.ok().build();

    }

    /**
     * 查询购物车列表
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList() {
        return ResponseEntity.ok(cartService.queryCartList());
    }

    /**
     * 修改购物车商品数量
     */
    @PutMapping
    public ResponseEntity<Void> updateCart(@RequestParam("id") Long skuId, @RequestParam("num") Integer num) {
        cartService.updateCart(skuId, num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除购物车
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> delCart(@PathVariable("skuId") String skuId) {
        cartService.delCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @PostMapping("demo")
    public ResponseEntity<Void> addCart222(@RequestBody List<Cart> cart) {
        cartService.addCart(cart);
        return ResponseEntity.ok().build();

    }
}
