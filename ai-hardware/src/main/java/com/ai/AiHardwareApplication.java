package com.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 潘越
 */
@SpringBootApplication
@MapperScan("com.ai.mapper")
public class AiHardwareApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiHardwareApplication.class, args);
    }

}