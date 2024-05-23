package com.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 刘晨
 */
@SpringBootApplication
@MapperScan("com.ai.mapper")
public class AiBackgroundApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiBackgroundApplication.class, args);
    }

}
