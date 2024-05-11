package com.github.ecommercemall.ecommercemallproduct;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.github.ecommercemall.ecommercemallproduct.feign")
@MapperScan("com.github.ecommercemall.ecommercemallproduct.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class ECommerceMallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallProductApplication.class, args);
    }

}
