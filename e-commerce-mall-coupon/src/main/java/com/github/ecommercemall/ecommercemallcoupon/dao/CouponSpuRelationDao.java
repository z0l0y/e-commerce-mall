package com.github.ecommercemall.ecommercemallcoupon.dao;

import com.github.ecommercemall.ecommercemallcoupon.entity.CouponSpuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 * 
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 18:53:46
 */
@Mapper
public interface CouponSpuRelationDao extends BaseMapper<CouponSpuRelationEntity> {
	
}
