package com.leyou.common.dto;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @NotNull
    private Long addressId; // 收获人地址id
    @NotNull
    private Integer paymentType;// 付款类型
    @NotNull
    private List<CartDTO> carts;// 订单详情
}