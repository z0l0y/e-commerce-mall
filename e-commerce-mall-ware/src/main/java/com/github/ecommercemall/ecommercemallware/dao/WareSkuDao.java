package com.github.ecommercemall.ecommercemallware.dao;

import com.github.ecommercemall.ecommercemallware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 19:12:51
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    // 注意一定要写@Param，要不然很有可能会出现Bug
    Long getSkuStock(@Param("skuId") Long skuId);
}
