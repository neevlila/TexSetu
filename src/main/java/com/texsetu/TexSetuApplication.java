package com.texsetu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TexSetuApplication {

    public static void main(String[] args) {
        SpringApplication.run(TexSetuApplication.class, args);
    }
}
