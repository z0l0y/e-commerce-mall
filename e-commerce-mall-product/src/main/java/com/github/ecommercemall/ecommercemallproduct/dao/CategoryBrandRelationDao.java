package com.github.ecommercemall.ecommercemallproduct.dao;

import com.github.ecommercemall.ecommercemallproduct.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 12:30:49
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    void updateCategory(@Param("catId") Long catId, @Param("name") String name);
}
