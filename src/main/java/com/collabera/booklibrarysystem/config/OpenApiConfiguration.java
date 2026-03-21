package com.collabera.booklibrarysystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI libraryOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Library System API")
                .version("v1")
                .description("REST API for managing borrowers, books, and book borrowing operations.")
                .contact(new Contact().name("Collabera Assessment"))
                .license(new License().name("Internal Use")));
    }
}
