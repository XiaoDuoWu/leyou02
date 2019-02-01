package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.dto.OrderDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.GoodsClient;
import com.leyou.item.pojo.Sku;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.filter.LoginInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.pojo.AddressDTO;
import com.leyou.userinterface.AddressClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private AddressClient addressClient;

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        // 1 写order
        Order order = new Order();

        // 1.1 订单编号   使用雪花算法 填充orderId
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);

        // 1.2 登录用户  获取用户信息 填充
        UserInfo user = LoginInterceptor.getLoginUser();
        System.err.println(user.getId());
        order.setUserId(user.getId());//用户的id
        order.setBuyerRate(false);//是否评价
        order.setBuyerNick(user.getUsername());//用户的昵称

        // 1.3 收件人信息
        AddressDTO address = addressClient.queryAddressById(orderDTO.getAddressId());
//        填充物流数据
        fillAddressInOrder(order, address);

        // 1.4 金额相关信息  获取前端获取的购物车中商品数据
        List<CartDTO> carts = orderDTO.getCarts();
        // 使用流处理获取skuId的list集合
        List<Long> idList = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        // 处理CartDTO为一个map， 其key是skuId；值是num
        Map<Long, Integer> numMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        // 1.4.1 根据skuId查询sku
        List<Sku> skuList = goodsClient.querySkuByIds(idList);
        // 定义一个OrderDetail的集合
        List<OrderDetail> details = new ArrayList<>();
        // 1.4.2 计算金额的和
        long total = 0;
        for (Sku sku : skuList) {
            int num = numMap.get(sku.getId());
            // 计算总金额
            total += sku.getPrice() * num;
            // 组装OrderDetail
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setNum(num);
            detail.setSkuId(sku.getId());
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }
        // 1.4.3 填写金额数据
        order.setTotalPay(total);
        order.setPaymentType(orderDTO.getPaymentType());
        order.setActualPay(total + order.getPostFee()/* - 优惠金额*/);

        // 1.5 其他默认字段
        order.setCreateTime(new Date());

        // 1.6 写order
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            throw new LyException(ExceptionEnum.ORDER_INSERT_ERROR);
        }

        // 2 写OrderDetail
        count = detailMapper.insertList(details);
        if (count != details.size()) {
            throw new LyException(ExceptionEnum.ORDER_INSERT_ERROR);
        }

        // 3 写orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(order.getCreateTime());
        count = statusMapper.insertSelective(orderStatus);
        if (count != 1) {
            throw new LyException(ExceptionEnum.ORDER_INSERT_ERROR);
        }

        // 4 减库存
        goodsClient.decreaseStock(carts);
        Map<Long, List<Long>> map = new HashMap<>();
        map.put(user.getId(), idList);
//        减购物车中的商品
        sendMessage(map, "delete");
        return orderId;
    }

    private void sendMessage( Map<Long, List<Long>> map, String delete) {
        // 发送消息
        try {
            this.amqpTemplate.convertAndSend("cart." + delete, map);
            System.err.println("消息发出了");
        } catch (Exception e) {
            log.error("购物车减 异常：{}"+ delete, map, e);
        }
    }

    /**
     * 物流数据填充在order对象中
     *
     * @param order   对象
     * @param address 存放着物流数据
     */
    private void fillAddressInOrder(Order order, AddressDTO address) {
        order.setReceiver(address.getName());
        order.setReceiverState(address.getState());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverMobile(address.getPhone());
        order.setReceiverZip(address.getZipCode());
    }

    public Order queryOrderById(Long id) {
        // 查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            // 不存在
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = detailMapper.select(detail);
        if(CollectionUtils.isEmpty(details)){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);

        // 查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            // 不存在
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

}
