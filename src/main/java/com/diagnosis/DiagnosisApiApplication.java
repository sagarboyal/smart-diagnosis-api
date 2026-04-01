package com.diagnosis;

import com.diagnosis.ai.config.GroqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GroqProperties.class)
public class DiagnosisApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiagnosisApiApplication.class, args);
    }
}











