package dev.kzone.portfolio.userapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Directory API",
                version = "1.1.0",
                description = "Searchable user directory REST API with validation, pagination and Excel export"
        )
)
public class OpenApiConfig {
}
