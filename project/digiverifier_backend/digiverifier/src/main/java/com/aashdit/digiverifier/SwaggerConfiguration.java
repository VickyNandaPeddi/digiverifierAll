package com.aashdit.digiverifier;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digiverifier")
                        .description("Background verification")
                        .version("0.0.1-SNAPSHOT"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("digiverifier-public")
                .packagesToScan("com.aashdit")  // scan all packages under com.aashdit.*
                .pathsToMatch("/**")             // include all paths
                .build();
    }
}
