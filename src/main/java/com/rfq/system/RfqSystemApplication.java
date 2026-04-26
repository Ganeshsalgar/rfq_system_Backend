package com.rfq.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RfqSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(RfqSystemApplication.class, args);
    }
}
