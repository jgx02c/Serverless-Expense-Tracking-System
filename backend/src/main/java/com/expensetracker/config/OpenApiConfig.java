package com.expensetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI expenseTrackerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Expense Tracker API")
                .description("API for managing expenses in a serverless environment")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Expense Tracker Team")
                    .email("support@expensetracker.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("https://api.expensetracker.com")
                    .description("Production Server"),
                new Server()
                    .url("https://staging-api.expensetracker.com")
                    .description("Staging Server")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token obtained from Cognito")));
    }
} 