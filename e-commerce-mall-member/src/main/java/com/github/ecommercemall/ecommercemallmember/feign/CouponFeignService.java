package com.github.ecommercemall.ecommercemallmember.feign;

import com.github.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("e-commerce-mall-coupon")
public interface CouponFeignService {
    @RequestMapping("ecommercemallcoupon/coupon/member/list")
    public R memberCoupons();
}
