package org.example.matcheat.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {
    static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI matchEatOpenApi() {
        return new OpenAPI()
                .info(new Info().title("MatchEAT API Documentation").version("v1.0"))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    OpenApiCustomizer bearerAuthOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, pathItem) -> {
                pathItem.readOperationsMap().forEach((method, operation) -> {
                    if (isProtectedApi(path, method)) {
                        operation.setSecurity(
                                java.util.List.of(new SecurityRequirement().addList(BEARER_AUTH)));
                    }
                });
            });
        };
    }

    private static boolean isProtectedApi(String path, io.swagger.v3.oas.models.PathItem.HttpMethod method) {
        if (!path.startsWith("/api/") || path.startsWith("/api/v1/auth/")) {
            return false;
        }
        return method != io.swagger.v3.oas.models.PathItem.HttpMethod.GET
                || !(path.equals("/api/v1/products")
                || path.equals("/api/v1/products/search")
                || path.equals("/api/v1/products/{id}"));
    }
}
