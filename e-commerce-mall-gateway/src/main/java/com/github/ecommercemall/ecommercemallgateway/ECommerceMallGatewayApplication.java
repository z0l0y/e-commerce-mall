package com.github.ecommercemall.ecommercemallgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ECommerceMallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallGatewayApplication.class, args);
    }

}
