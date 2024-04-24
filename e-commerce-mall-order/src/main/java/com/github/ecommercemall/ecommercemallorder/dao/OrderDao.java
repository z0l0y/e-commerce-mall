package com.github.ecommercemall.ecommercemallorder.dao;

import com.github.ecommercemall.ecommercemallorder.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 19:09:31
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
