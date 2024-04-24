package com.github.ecommercemall.ecommercemallcoupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ECommerceMallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallCouponApplication.class, args);
    }

}
