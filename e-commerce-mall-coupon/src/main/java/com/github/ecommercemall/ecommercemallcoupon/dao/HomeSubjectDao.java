package com.github.ecommercemall.ecommercemallcoupon.dao;

import com.github.ecommercemall.ecommercemallcoupon.entity.HomeSubjectEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 * 
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 18:53:46
 */
@Mapper
public interface HomeSubjectDao extends BaseMapper<HomeSubjectEntity> {
	
}
