package com.github.common.to;

import com.github.common.entity.OrderEntity;
import com.github.common.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /**
     * 订单计算的应付价格
     **/
    private BigDecimal payPrice;

    /**
     * 运费
     **/
    private BigDecimal fare;

}