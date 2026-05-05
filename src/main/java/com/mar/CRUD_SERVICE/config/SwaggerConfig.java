package com.mar.CRUD_SERVICE.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Tên của security scheme dùng JWT Bearer Token
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Social Network API")
                        .version("1.0.0")
                        .description(
                                "Hệ thống mạng xã hội dạng monolithic — Đồ án tốt nghiệp.\n\n" +
                                "**Hướng dẫn sử dụng:**\n" +
                                "1. Gọi `POST /api/v1/auth/authenticate` để lấy JWT Token\n" +
                                "2. Nhấn nút **Authorize** (🔒) ở góc phải trên\n" +
                                "3. Nhập token theo định dạng: `Bearer <token>`\n" +
                                "4. Sau đó có thể gọi tất cả các API cần xác thực"
                        )
                        .contact(new Contact()
                                .name("CRUD-SERVICE")
                                .email("admin@crudservice.com")
                        )
                )
                // Thêm nút Authorize để nhập JWT Token trực tiếp trên Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT Token nhận được sau khi đăng nhập")
                        )
                );
    }
}
