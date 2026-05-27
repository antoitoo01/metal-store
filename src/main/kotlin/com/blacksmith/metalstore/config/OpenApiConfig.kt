package com.blacksmith.metalstore.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Metal Store API")
                .description("ERP para talleres metalúrgicos — multi-tenant SaaS")
                .version("0.0.1")
        )
}
