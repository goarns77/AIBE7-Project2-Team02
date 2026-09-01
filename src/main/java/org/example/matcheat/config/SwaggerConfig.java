package org.example.matcheat.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		// 1. 보안 체계 이름 정의
		String securitySchemeName = "bearerAuth";

		// 2. SecurityRequirement 설정 (전역으로 인증 요구사항 적용)
		SecurityRequirement securityRequirement = new SecurityRequirement()
				.addList(securitySchemeName);

		// 3. SecurityScheme 설정 (Bearer 토큰 방식)
		Components components = new Components()
				.addSecuritySchemes(securitySchemeName, new SecurityScheme()
						.name(securitySchemeName)
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT"));

		return new OpenAPI()
				.info(new Info()
						.title("API Documentation")
						.version("v1.0"))
				.addSecurityItem(securityRequirement)
				.components(components);
	}
}