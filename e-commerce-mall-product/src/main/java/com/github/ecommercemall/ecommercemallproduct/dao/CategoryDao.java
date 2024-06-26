package com.github.ecommercemall.ecommercemallproduct.dao;

import com.github.ecommercemall.ecommercemallproduct.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 12:30:49
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
