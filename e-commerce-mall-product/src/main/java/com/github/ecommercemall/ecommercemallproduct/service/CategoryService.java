package com.github.ecommercemall.ecommercemallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.common.utils.PageUtils;
import com.github.ecommercemall.ecommercemallproduct.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 12:30:49
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    /**
     * 找到catelogId的完整路径
     * [父/子/孙]
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);
}

