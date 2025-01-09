package org.example;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@OpenAPIDefinition(info = @Info(
        title = "Deal Service API",
        version = "1.0",
        description = "API для работы с кредитными заявками"
))
public class DealServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DealServiceApplication.class, args);
    }
}
