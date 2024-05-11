package com.github.ecommercemall.ecommercemallproduct.feign;

import com.github.common.to.es.SkuEsModel;
import com.github.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("e-commerce-mall-search")
public interface SearchFeignService {

    @PostMapping(value = "/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);

}