package com.github.ecommercemall.ecommercemallthirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ECommerceMallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallThirdPartyApplication.class, args);
    }

}
