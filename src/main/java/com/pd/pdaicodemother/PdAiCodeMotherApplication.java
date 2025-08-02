package com.pd.pdaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pd.pdaicodemother.mapper")
public class PdAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdAiCodeMotherApplication.class, args);
    }

}
