package com.github.ecommercemall.ecommercemallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.ecommercemall.ecommercemallproduct.dao.BrandDao;
import com.github.ecommercemall.ecommercemallproduct.dao.CategoryDao;
import com.github.ecommercemall.ecommercemallproduct.entity.BrandEntity;
import com.github.ecommercemall.ecommercemallproduct.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.common.utils.PageUtils;
import com.github.common.utils.Query;

import com.github.ecommercemall.ecommercemallproduct.dao.CategoryBrandRelationDao;
import com.github.ecommercemall.ecommercemallproduct.entity.CategoryBrandRelationEntity;
import com.github.ecommercemall.ecommercemallproduct.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        final Long brandId = categoryBrandRelation.getBrandId();
        final Long catelogId = categoryBrandRelation.getCatelogId();
        // 查询详细名字
        final BrandEntity brandEntity = brandDao.selectById(brandId);
        final CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        // 设置详细名字
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        // 保存信息
        // 这里我们做了冗余存储，为了保证数据一致性，我们还要做一些处理
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        final CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        this.update(categoryBrandRelationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(CategoryEntity category) {

    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId,name);
    }

}