package com.shawn.wealth.rag.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 *
 * This class defines the API metadata such as title, version, description,
 * and contact information, which are used to generate interactive API documentation
 * via Swagger UI.
 *
 * It helps standardize API documentation and provides a clear contract for
 * consumers, improving API discoverability and maintainability in a microservices environment.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wealthRagOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wealth Knowledge RAG API")
                        .version("v1")
                        .description("Spring Boot + Spring AI + Ollama + Qdrant APIs")
                        .contact(new Contact()
                                .name("Shawn")
                                .email("shawn@test.com")));
    }
}
