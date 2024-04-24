package com.github.ecommercemall.ecommercemallorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ECommerceMallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallOrderApplication.class, args);
    }

}
