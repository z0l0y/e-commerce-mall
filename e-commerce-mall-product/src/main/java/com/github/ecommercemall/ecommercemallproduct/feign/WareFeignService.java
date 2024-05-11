package com.github.ecommercemall.ecommercemallproduct.feign;

import com.github.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("e-commerce-mall-ware")
public interface WareFeignService {
    @PostMapping(value = "ecommercemallware/waresku/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds);
}
