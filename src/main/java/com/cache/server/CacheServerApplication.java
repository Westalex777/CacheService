package com.cache.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class CacheServerApplication {

    public static final UUID ID = UUID.randomUUID();

    public static void main(String[] args) {
        SpringApplication.run(CacheServerApplication.class, args);
    }

}
