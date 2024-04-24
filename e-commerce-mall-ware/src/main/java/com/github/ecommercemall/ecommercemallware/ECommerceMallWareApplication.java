package com.github.ecommercemall.ecommercemallware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ECommerceMallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallWareApplication.class, args);
    }

}
