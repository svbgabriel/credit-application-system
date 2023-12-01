package me.dio.credit.application.system.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun publicApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("springcreditapplicationsystem-public")
        .pathsToMatch("/api/customers/**", "/api/credits/**")
        .build()
}