package com.github.ecommercemall.ecommercemallproduct.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.github.common.constant.ProductConstant;
import com.github.common.to.SkuHasStockVo;
import com.github.common.to.es.SkuEsModel;
import com.github.common.utils.R;
import com.github.ecommercemall.ecommercemallproduct.entity.*;
import com.github.ecommercemall.ecommercemallproduct.feign.SearchFeignService;
import com.github.ecommercemall.ecommercemallproduct.feign.WareFeignService;
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
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFeignService searchFeignService;

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
        List<Long> skuIdList = skuInfoEntities.stream()
                .map(SkuInfoEntity::getSkuId)
                .collect(Collectors.toList());
        // TODO 发送远程调用，确认是否有库存
        Map<Long, Boolean> stockMap = null;
        // 由于是远程调用，可能会网络波动导致不稳定，可能会导致失败。
        // 一旦调用失败我们没有try的话，会导致下面的都不走了，使用try可以使下面的保存仍然可以执行
        try {
            R skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            //
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
            };
            stockMap = skuHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }

        // 2、封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(sku -> {
            // 组装需要的数据 skuModel
            SkuEsModel skuModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuModel);
            skuModel.setSkuPrice(sku.getPrice());
            skuModel.setSkuImg(sku.getSkuDefaultImg());
            // 设置库存信息
            if (finalStockMap == null) {
                skuModel.setHasStock(true);
            } else {
                skuModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

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
        // 将数据发送给检索服务进行保存 e-commerce-mall-search
        // TODO 5、将数据发给es进行保存：gulimall-search
        R r = searchFeignService.productStatusUp(upProducts);

        if (r.getCode() == 0) {
            // 远程调用成功
            // TODO 6、修改当前spu的状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.ProductStatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO 7、重复调用？接口幂等性:重试机制,这个和我们feign的底层有关，因为它在失败之后会进行重试。
            //  想象我们之前部署kubernetes是不是有一个metric,是不是在我们失败之后重试了
        }
    }
}