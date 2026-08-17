package com.example.foodie.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SECURITY_SCHEME = "bearerAuth";

    @Value("${app.version}")
    private String appVersion;

    @Bean
    public OpenAPI foodieOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Foodie API")
                        .description("REST API cho hệ thống đặt món ăn Foodie: xác thực người dùng, " +
                                "nhà hàng, món ăn, giỏ hàng, đơn hàng và đánh giá.")
                        .version(appVersion))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SECURITY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
