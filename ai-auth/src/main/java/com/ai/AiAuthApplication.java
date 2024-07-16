package com.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * @author 刘晨
 */
@SpringBootApplication
@MapperScan("com.ai.mapper")
@EnableFeignClients
@EnableScheduling
public class AiAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAuthApplication.class, args);
    }

}
