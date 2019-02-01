package com.leyou.order.mapper;

import com.leyou.order.pojo.OrderDetail;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;
public interface OrderDetailMapper extends InsertListMapper<OrderDetail>,Mapper<OrderDetail> {
}