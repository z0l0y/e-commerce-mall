package com.github.ecommercemall.ecommercemallproduct.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.ecommercemall.ecommercemallproduct.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.common.utils.PageUtils;
import com.github.common.utils.Query;

import com.github.ecommercemall.ecommercemallproduct.dao.BrandDao;
import com.github.ecommercemall.ecommercemallproduct.entity.BrandEntity;
import com.github.ecommercemall.ecommercemallproduct.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("brand_id", key).or().like("name", key);
        }

        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                new QueryWrapper<BrandEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        // 保证冗余数据字段的一致性
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            // 同步更新其他关联表中的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
            // TODO 更新其他关联
        }
    }

}