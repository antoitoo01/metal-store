package com.blacksmith.metalstore.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "BearerJwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Supabase JWT — incluir en header: Authorization: Bearer {token}"
)
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Metal Store API")
                .description("ERP para talleres metalúrgicos — multi-tenant SaaS")
                .version("0.0.1")
        )
        .addServersItem(Server().url("/").description("Relative — usa el mismo host de la request"))
        .addSecurityItem(SecurityRequirement().addList("BearerJwt"))
        .tags(
            listOf(
                Tag().name("Auth").description("Autenticación y gestión de usuarios"),
                Tag().name("Catalog").description("Perfiles, items, familias y tipos de catálogo"),
                Tag().name("Clients").description("Gestión de clientes"),
                Tag().name("Quotes").description("Presupuestos y cotizaciones"),
                Tag().name("Billing").description("Facturación, precios y líneas"),
                Tag().name("Inventory").description("Control de stock e inventario")
            )
        )
}
