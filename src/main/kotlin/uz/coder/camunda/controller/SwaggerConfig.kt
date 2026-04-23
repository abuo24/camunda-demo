package uz.coder.camunda.controller

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Camunda Demo API")
                .description("Spring Boot + Kotlin + Camunda REST API")
                .version("1.0.0")
                .contact(
                    Contact()
                        .name("Demo")
                        .email("demo@mail.com")
                )
        )
        .servers(
            listOf(
                Server().url("http://localhost:8090").description("Local")
            )
        )
}
 