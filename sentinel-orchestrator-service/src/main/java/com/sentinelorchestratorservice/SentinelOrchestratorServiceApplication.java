package com.sentinelorchestratorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class SentinelOrchestratorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SentinelOrchestratorServiceApplication.class, args);
    }
}
