package com.franck.amiinvisible;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AmiInvisibleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmiInvisibleApplication.class, args);
    }

}
