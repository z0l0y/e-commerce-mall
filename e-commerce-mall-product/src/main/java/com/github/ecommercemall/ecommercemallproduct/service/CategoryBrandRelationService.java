package com.github.ecommercemall.ecommercemallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.common.utils.PageUtils;
import com.github.ecommercemall.ecommercemallproduct.entity.CategoryBrandRelationEntity;
import com.github.ecommercemall.ecommercemallproduct.entity.CategoryEntity;

import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 12:30:49
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(CategoryEntity category);

    void updateCategory(Long catId, String name);
}

