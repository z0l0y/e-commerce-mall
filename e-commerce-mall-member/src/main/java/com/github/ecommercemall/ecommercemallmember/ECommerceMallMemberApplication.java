package com.github.ecommercemall.ecommercemallmember;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.github.ecommercemall.ecommercemallmember.feign")
@SpringBootApplication
public class ECommerceMallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallMemberApplication.class, args);
    }

}
