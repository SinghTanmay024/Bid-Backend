package com.bidbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BidBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidBackendApplication.class, args);
    }
}
