package com.farmosync.pdv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PdvServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PdvServiceApplication.class, args);
    }
}
