package com.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author 刘晨
 */
@SpringBootApplication
@EnableFeignClients
public class AiNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiNotifyApplication.class, args);
    }

}
