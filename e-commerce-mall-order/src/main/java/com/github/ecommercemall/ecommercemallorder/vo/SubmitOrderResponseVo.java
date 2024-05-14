package com.github.ecommercemall.ecommercemallorder.vo;

import com.github.ecommercemall.ecommercemallorder.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /**
     * 错误状态码
     **/
    private Integer code;


}