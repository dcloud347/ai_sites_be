package com.ai;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

@SpringBootTest
class AiAuthApplicationTests {

    @Test
    void gen() {
        FastAutoGenerator.create("jdbc:mysql://aisites.mysql.database.azure.com/ai", "ai_admin", "A1SiT3_$0rkSW4lLbl").globalConfig(builder -> builder.author("")
                        .outputDir("./src/main/java").build())
                .packageConfig(builder -> builder.parent("com")
                        .moduleName("ai").
                        pathInfo(Collections.singletonMap(OutputFile.xml, "./src/main/resources/mapper"))).
                strategyConfig(builder -> builder.addInclude("user")
                        .entityBuilder().
                        enableLombok().
                        enableChainModel()
                        .controllerBuilder()
                        .enableRestStyle())
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

}
