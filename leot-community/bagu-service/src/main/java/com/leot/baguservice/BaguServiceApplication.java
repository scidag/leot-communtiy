package com.leot.baguservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.leot.baguservice.mapper")
@SpringBootApplication
public class BaguServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaguServiceApplication.class, args);
    }

}
