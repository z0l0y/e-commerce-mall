package com.github.ecommercemall.ecommercemallcoupon.dao;

import com.github.ecommercemall.ecommercemallcoupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 18:53:46
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
