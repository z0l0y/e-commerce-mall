package com.github.ecommercemall.ecommercemallware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.common.utils.PageUtils;
import com.github.ecommercemall.ecommercemallware.entity.WareSkuEntity;
import com.github.ecommercemall.ecommercemallware.vo.SkuHasStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 19:12:51
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 判断是否有库存
     * @param skuIds
     * @return
     */
    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);
}

