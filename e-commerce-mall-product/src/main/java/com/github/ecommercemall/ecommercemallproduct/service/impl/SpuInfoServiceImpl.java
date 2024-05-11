package com.github.ecommercemall.ecommercemallproduct.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.github.common.to.es.SkuEsModel;
import com.github.ecommercemall.ecommercemallproduct.entity.*;
import com.github.ecommercemall.ecommercemallproduct.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.common.utils.PageUtils;
import com.github.common.utils.Query;

import com.github.ecommercemall.ecommercemallproduct.dao.SpuInfoDao;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListforspu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        // 找到具体的id
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = productAttrValueEntities.stream().filter(item -> idSet.contains(item.getAttrId()))
                .map(item -> {
                    final SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                })
                .collect(Collectors.toList());


        // 1.组装需要的数据
        SkuEsModel skuEsModel = new SkuEsModel();
        // 查出当前spu对应的sku的信息
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuModel);
            skuModel.setSkuPrice(sku.getPrice());
            skuModel.setSkuImg(sku.getSkuDefaultImg());
            // 发送远程调用，确认是否有库存
            skuModel.setHasStock(false);

            // 热度评分
            skuModel.setHotScore(0L);

            // 查询品牌和分类名字的信息
            BrandEntity brand = brandService.getById(sku.getBrandId());
            skuModel.setBrandName(brand.getName());
            skuModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(sku.getCatalogId());
            skuModel.setCatalogName(category.getName());

            // 设置检索属性
            skuModel.setAttrs(attrsList);
            return skuModel;
        }).collect(Collectors.toList());
    }
    // 将数据发送给检索服务进行保存 e-commerce-mall-search
}