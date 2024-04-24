package com.github.ecommercemall.ecommercemallware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.common.utils.PageUtils;
import com.github.ecommercemall.ecommercemallware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 19:12:51
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

