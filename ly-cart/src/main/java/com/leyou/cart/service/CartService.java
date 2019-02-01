package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.filter.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    static final String KEY_RREFIX = "ly:cart:uid:";

    /**
     * 新增购物车商品
     *
     * @param cart 购物车商品的具体信息
     */
    public void addCart(Cart cart) {
//        获取当前用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key = KEY_RREFIX + userInfo.getId();
//        获取商品Id
        String hashKey = cart.getSkuId().toString();
//        获取购物车单件商品的数量
        int num = cart.getNum();
//        获取hash操作的对象
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
//        判断 要添加的商品是否存在
        if (hashOps.hasKey(hashKey)) {
//            存在 , 修改数量
            JsonUtils.toBean(hashOps.get(hashKey).toString(), Cart.class);
//            如果之前这个用户购物车已经出现这件商品 那么就在商品数量的基础上 加上此次添加的数量即可
            cart.setNum(num + cart.getNum());
        }
//        写入redis中
        hashOps.put(hashKey, JsonUtils.toString(cart));
    }

    public List<Cart> queryCartList() {
//        获取登录用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
//        判断是否存在购物车
        String key = KEY_RREFIX + userInfo.getId();
        if (!redisTemplate.hasKey(key)) {
//            如果不存在 直接返回
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
//        获取购物车数据
        List<Object> carts = hashOps.values();
//        判断是否有数据
        if (CollectionUtils.isEmpty(carts)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
//        查询购物车数据
        List<Cart> cartList = carts.stream().map(c -> JsonUtils.toBean(c.toString(), Cart.class)).collect(Collectors.toList());
        return cartList;
    }

    /**
     * 修改购物车的具体信息
     *
     * @param skuId 商品的具体skuId
     * @param num   skuId对应商品的购物车具体数量
     */
    public void updateCart(Long skuId, Integer num) {
        try {
//        获取当前用户的信息
            UserInfo userInfo = LoginInterceptor.getLoginUser();
//        获取key加前缀
            String key = KEY_RREFIX + userInfo.getId();
//        获取hash操作的对象
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
//        如果sku在购物车中不存在 则抛异常
            if (!hashOps.hasKey(skuId.toString())) {
                throw new RuntimeException("购物车商品不存在，用户：" + userInfo.getId() + "，商品：" + skuId);
            }
            Cart cart = JsonUtils.toBean(hashOps.get(skuId.toString()).toString(), Cart.class);
//        修改数量
            cart.setNum(num);
//        重新写入redis中
            hashOps.put(skuId.toString(), JsonUtils.toString(cart));
        } catch (Exception e) {
            log.error("修改购物车", e);
        }
    }

    /**
     * 删除购物车中的商品
     *
     * @param skuId 商品的id
     */
    public void delCart(String skuId) {
//        获取用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key = KEY_RREFIX + userInfo.getId();
//        获取hashOps操作对象
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        redisTemplate.opsForHash().delete(key, skuId);
    }

    public void addCart(List<Cart> carts) {
//        获取当前用户的信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
//        获取key加前缀
        String key = KEY_RREFIX + userInfo.getId();
        for (Cart cart : carts) {
//        获取商品Id
            String hashKey = cart.getSkuId().toString();
//        获取购物车单件商品的数量
            int num = cart.getNum();
//        获取hash操作的对象
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
//        判断 要添加的商品是否存在
            if (hashOps.hasKey(hashKey)) {
//            存在 , 修改数量
                JsonUtils.toBean(hashOps.get(hashKey).toString(), Cart.class);
//            如果之前这个用户购物车已经出现这件商品 那么就在商品数量的基础上 加上此次添加的数量即可
                cart.setNum(num + cart.getNum());
            }
//        写入redis中
            hashOps.put(hashKey, JsonUtils.toString(cart));
        }
    }

//接收order服务 中删除 购物车的消息 进行处理
    public void deleteCarts(Map<Long, List<Long>> map) {
        delCartMq(map);

    }
// 删除购物车  map中 key是 用户的userId  value是 购物车id
    public void delCartMq(Map<Long, List<Long>> map) {
        for (Long userId : map.keySet()) {
            List<Long> skuList = map.get(userId);
            for (Long skuId : skuList) {
                String key = KEY_RREFIX + userId;
                // 获取hashOps操作对象
                BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
                redisTemplate.opsForHash().delete(key, skuId.toString());

            }
        }


    }
}
