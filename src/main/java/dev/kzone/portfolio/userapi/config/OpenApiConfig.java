package dev.kzone.portfolio.userapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Business Ops Dashboard API",
                version = "2.0.0",
                description = "Operations REST API with customer directory, work-order state transitions, validation, pagination and Excel export"
        )
)
public class OpenApiConfig {
}
